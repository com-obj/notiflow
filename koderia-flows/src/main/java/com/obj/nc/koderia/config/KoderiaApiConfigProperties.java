package com.obj.nc.koderia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "koderia.api")
public class KoderiaApiConfigProperties {

    private String uri;

}
