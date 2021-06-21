package com.obj.nc.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("nc.jwt")
public class NcJwtConfigProperties {
    
    private String username;
    private String password;
    private String signatureSecret;
    
}
