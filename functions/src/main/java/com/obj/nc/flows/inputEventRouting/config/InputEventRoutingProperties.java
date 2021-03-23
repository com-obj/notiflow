package com.obj.nc.flows.inputEventRouting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = InputEventRoutingProperties.CONFIG_PROPS_PREFIX)
public class InputEventRoutingProperties {
	
	public static final String CONFIG_PROPS_PREFIX = "nc.flows.input-evet-routing";

    private int pollPeriodInMiliSeconds=1000;
    
    private boolean enabled = false;

}
