package com.obj.nc.functions.processors.messageTemplating.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("nc.functions.email-tracking")
public class EmailTrackingConfigProperties {
    
    private boolean enabled = false;
    
}
