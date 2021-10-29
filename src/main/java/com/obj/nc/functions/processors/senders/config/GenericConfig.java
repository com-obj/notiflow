package com.obj.nc.functions.processors.senders.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GenericConfig {
    public static final String GENERIC_REST_TEMPLATE = "GENERIC_REST_TEMPLATE";

    @Bean
    @Qualifier(GENERIC_REST_TEMPLATE)
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
