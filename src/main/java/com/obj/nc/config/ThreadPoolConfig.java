package com.obj.nc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadPoolConfig {

    @Autowired NcAppConfigProperties ncAppConfigProperties;

    @Bean(name = "threadPoolTaskExecutor")
    public TaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(ncAppConfigProperties.getCorePoolSize());
        executor.setMaxPoolSize(ncAppConfigProperties.getMaxPoolSize());
        executor.initialize();
        return executor;
    }
}
