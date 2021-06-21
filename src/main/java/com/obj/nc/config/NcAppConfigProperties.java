package com.obj.nc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("nc.app")
public class NcAppConfigProperties {
    
    private String url;
    
}
