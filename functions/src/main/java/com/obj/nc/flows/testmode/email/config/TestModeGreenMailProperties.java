package com.obj.nc.flows.testmode.email.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "nc.flows.test-mode.green-mail")
public class TestModeGreenMailProperties {

    private int smtpPort = 4025;
    
    private int imapPort = 4143;

}
