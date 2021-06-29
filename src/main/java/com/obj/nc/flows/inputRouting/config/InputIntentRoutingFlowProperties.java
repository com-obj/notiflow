package com.obj.nc.flows.inputRouting.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = InputIntentRoutingFlowProperties.CONFIG_PROPS_PREFIX)
public class InputIntentRoutingFlowProperties {
		
	public enum RoutingType {
		FLOW_ID, PAYLOAD_TYPE
	}
	
	public static final String CONFIG_PROPS_PREFIX = "nc.flows.input-intent-routing";

    private int pollPeriodInMilliSeconds = 1000;
    
    private RoutingType type = RoutingType.FLOW_ID;
    
    /*if type = PAYLOAD_TYPE. Map<son @type value, channel name>*/
    private Map<String,String> typeChannelMapping;
    
    private String typePropertyName = "@type";

}
