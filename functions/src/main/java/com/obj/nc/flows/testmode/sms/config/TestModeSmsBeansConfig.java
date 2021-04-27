package com.obj.nc.flows.testmode.sms.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.obj.nc.flows.testmode.sms.funcitons.processors.InMemorySmsSender;
import com.obj.nc.flows.testmode.sms.funcitons.sources.InMemorySmsSourceSupplier;
import com.obj.nc.functions.processors.senders.SmsSender;

@Configuration
@ConditionalOnProperty(value = "nc.flows.test-mode.enabled", havingValue = "true")
public class TestModeSmsBeansConfig {
	
    /**
     * The provided smsSender service override. Instead of performing real SMS delivery, this implementation sends sms
     * to in memory store which is then used to create digest email
     * @param properties
     * @return
     */
    @Bean
    @Primary
    public SmsSender smsSender() {
        return new InMemorySmsSender(smsReciever());
    }
    
    @Bean
    @Primary
    public InMemorySmsSourceSupplier smsReciever() {
        return new InMemorySmsSourceSupplier();

    }

    
}
