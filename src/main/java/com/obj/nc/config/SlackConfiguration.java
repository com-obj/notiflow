package com.obj.nc.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("nc.slack")
public class SlackConfiguration {
    private String apiUrl;
    private String botToken;
}
