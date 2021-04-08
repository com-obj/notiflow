package com.obj.nc.osk.functions.processors.eventConverter.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@ConfigurationProperties(prefix = "osk.sia.functions.impact-notif-converter")
@Data
@Validated
@Configuration
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotifEventConverterConfigProperties {

	@Singular("b2bLoginNotify")
	private List<String> b2bLoginOfLACustumersToBeNotified;
	@Singular("agentsNotifEmail")
	private Set<String> csAgentsToNotifyEmail;
	
	public List<String> getB2bLoginOfLACustumersToBeNotified() {
		if (this.b2bLoginOfLACustumersToBeNotified == null) {
			this.b2bLoginOfLACustumersToBeNotified = new ArrayList<String>();
		}

		return b2bLoginOfLACustumersToBeNotified;
	}
	
	public Set<String> getCsAgentsToNotifyEmail() {
		if (this.csAgentsToNotifyEmail == null) {
			this.csAgentsToNotifyEmail = new HashSet<String>();
		}

		return csAgentsToNotifyEmail;
	}
}