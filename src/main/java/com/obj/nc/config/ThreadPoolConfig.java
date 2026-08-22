package com.obj.nc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig {

    public static final String MESSAGE_PROCESSING_TASK_EXECUTOR = "threadPoolTaskExecutor";
    public static final String MESSAGE_DISPATCH_TASK_EXECUTOR = "messageDispatchTaskExecutor";
    public static final String DELIVERY_INFO_TASK_EXECUTOR = "deliveryInfoTaskExecutor";
    public static final String EVENT_POLLING_TASK_EXECUTOR = "eventPollingTaskExecutor";
    public static final String SMS_SENDING_TASK_EXECUTOR = "smsSendingTaskExecutor";
    public static final String ASYNC_FLOW_TASK_EXECUTOR = "asyncFlowTaskExecutor";
    public static final String EMAIL_SENDING_TASK_EXECUTOR = "emailSendingTaskExecutor";

    @Autowired NcAppConfigProperties ncAppConfigProperties;

    @Primary
    @Bean(name = MESSAGE_PROCESSING_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        return boundedTaskExecutor("nc-message-input-");
    }

    @Bean(name = MESSAGE_DISPATCH_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor messageDispatchTaskExecutor() {
        return boundedTaskExecutor("nc-message-dispatch-");
    }

    @Bean(name = DELIVERY_INFO_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor deliveryInfoTaskExecutor() {
        return boundedTaskExecutor("nc-delivery-info-");
    }

    @Bean(name = SMS_SENDING_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor smsSendingTaskExecutor() {
        return boundedTaskExecutor(
                "nc-sms-sender-",
                ncAppConfigProperties.getSmsSendingCorePoolSize(),
                ncAppConfigProperties.getSmsSendingMaxPoolSize(),
                ncAppConfigProperties.getSmsSendingQueueCapacity());
    }

    @Bean(name = ASYNC_FLOW_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor asyncFlowTaskExecutor() {
        return boundedTaskExecutor("nc-async-flow-");
    }

    @Bean(name = EMAIL_SENDING_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor emailSendingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("nc-email-sender-");
        // Poll triggers are disposable; the email itself remains durable in the QueueChannel.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }

    @Bean(name = EVENT_POLLING_TASK_EXECUTOR)
    public ThreadPoolTaskExecutor eventPollingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1);
        executor.setThreadNamePrefix("nc-event-poller-");
        // A skipped poll is safe: the event remains durable and the next poll can pick it up.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }

    private ThreadPoolTaskExecutor boundedTaskExecutor(String threadNamePrefix) {
        return boundedTaskExecutor(
                threadNamePrefix,
                ncAppConfigProperties.getCorePoolSize(),
                ncAppConfigProperties.getMaxPoolSize(),
                ncAppConfigProperties.getQueueCapacity());
    }

    private ThreadPoolTaskExecutor boundedTaskExecutor(
            String threadNamePrefix, int corePoolSize, int maxPoolSize, int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        // Keep the queue bounded, but push overflow work back to the producer instead of
        // timing out and losing a message from an event that is already marked consumed.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
