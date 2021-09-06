package com.obj.nc.functions.processors.messageTemplating.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties("nc.functions.email-tracking")
public class EmailTrackingConfigProperties {
    
    private boolean enabled = false;
    
}
