package com.obj.nc.osk.functions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "osk.sia.functions.impact-notif-converter")
@Data
public class NotifEventConverterConfig {

	private List<String> b2bLoginOfLACustumersToBeNotified = new ArrayList<>();
	private Set<String> csAgentsToNotifyEmail = new HashSet<String>();
}
