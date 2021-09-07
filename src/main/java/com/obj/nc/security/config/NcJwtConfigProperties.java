package com.obj.nc.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties("nc.jwt")
public class NcJwtConfigProperties {
    
    private String username;
    private String password;
    private String signatureSecret;
    
}
