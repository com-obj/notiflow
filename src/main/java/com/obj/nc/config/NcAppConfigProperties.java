package com.obj.nc.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("nc.app")
public class NcAppConfigProperties {
    
    private String url;
    private boolean checkReferenceIntegrity = false;
    
    @Value("${nc.app.url.context-path:/notiflow}")
    private String contextPath;
    
}
