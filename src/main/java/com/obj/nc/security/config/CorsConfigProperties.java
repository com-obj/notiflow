package com.obj.nc.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

@Data
@Configuration
@ConfigurationProperties("cors")
public class CorsConfigProperties {
    private List<String> allowedOrigins = new ArrayList<>();
    private List<String> allowedMethods = singletonList(CorsConfiguration.ALL);
    private List<String> allowedHeaders = singletonList(CorsConfiguration.ALL);
}
