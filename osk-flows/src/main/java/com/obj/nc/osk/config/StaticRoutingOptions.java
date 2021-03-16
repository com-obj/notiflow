package com.obj.nc.osk.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "osk.sia.impact-notif")
@Data
public class StaticRoutingOptions {

	private List<String> b2bLoginOfLACustumersToBeNotified = new ArrayList<>();
}
