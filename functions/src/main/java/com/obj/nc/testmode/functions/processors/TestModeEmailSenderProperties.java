package com.obj.nc.testmode.functions.processors;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "testmode.smtp")
public class TestModeEmailSenderProperties {

    private String host;

    private int port;

    private String username;

    private String password;

    private String recipient;

    private int periodMinutes;

}
