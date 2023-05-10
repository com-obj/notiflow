package com.obj.nc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.util.CallerBlocksPolicy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    @Autowired NcAppConfigProperties ncAppConfigProperties;

    @Primary
    @Bean(name = "threadPoolTaskExecutor")
    public TaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(ncAppConfigProperties.getCorePoolSize());
        executor.setMaxPoolSize(ncAppConfigProperties.getMaxPoolSize());
        executor.setQueueCapacity(ncAppConfigProperties.getQueueCapacity());
        executor.setRejectedExecutionHandler(new CallerBlocksPolicy(TimeUnit.SECONDS.toMillis(5)));
        executor.initialize();
        return executor;
    }
}
