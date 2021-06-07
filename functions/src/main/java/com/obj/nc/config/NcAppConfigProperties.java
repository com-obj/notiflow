package com.obj.nc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotEmpty;

@Data
@ConfigurationProperties("nc.app")
@Configuration
public class NcAppConfigProperties {
    
    @NotEmpty private String url;
    
}
