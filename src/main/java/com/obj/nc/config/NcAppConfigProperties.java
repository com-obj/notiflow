package com.obj.nc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties("nc.app")
public class NcAppConfigProperties {
    
    private String url;
    private boolean checkReferenceIntegrity = false;
    
    @Value("${nc.app.url.context-path:/notiflow}")
    private String contextPath;
    
}
