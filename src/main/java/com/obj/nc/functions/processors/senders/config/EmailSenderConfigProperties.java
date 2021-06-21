package com.obj.nc.functions.processors.senders.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties(prefix = "nc.functions.email-sender")
@Data
@Component
public class EmailSenderConfigProperties {

	private String fromMailAddress;
}
