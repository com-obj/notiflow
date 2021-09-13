package com.obj.nc.functions.processors.messageTemplating.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties("nc.functions.tracking")
public class TrackingConfigProperties {
    
    private boolean enabled = false;
    
}
