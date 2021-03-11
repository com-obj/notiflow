package com.obj.nc.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "smsapi")
public class SmsClientConfigProperties {

    private String uri;

}
