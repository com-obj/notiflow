package com.obj.nc.flows.inputRouting.config;

import com.obj.nc.flows.inputRouting.FlowId2InputMessageRouter;
import com.obj.nc.flows.inputRouting.SimpleTypeBasedMessageRouter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutersConfig {
    
    @Bean
    public SimpleTypeBasedMessageRouter simplePayloadTypeBasedRouter() {
        return new SimpleTypeBasedMessageRouter();
    }
    
    @Bean
    public FlowId2InputMessageRouter flowIdRouter() {
        return new FlowId2InputMessageRouter();
    }
    
}
