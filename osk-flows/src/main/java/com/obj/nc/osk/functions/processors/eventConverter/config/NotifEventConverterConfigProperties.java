package com.obj.nc.osk.functions.processors.eventConverter.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@ConfigurationProperties(prefix = "osk.sia.functions.impact-notif-converter")
@Data
@Validated
@Configuration
public class NotifEventConverterConfigProperties {

	private List<String> b2bLoginOfLACustumersToBeNotified = new ArrayList<>();
	private Set<String> csAgentsToNotifyEmail = new HashSet<String>();
}