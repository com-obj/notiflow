package com.obj.nc.testmode.functions.processors;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "testmode.smtp")
public class TestModeEmailSenderProperties {

    private String host;

    private int port;

    private String username;

    private String password;

    private List<String> recipients;

    private int periodMinutes;

}
