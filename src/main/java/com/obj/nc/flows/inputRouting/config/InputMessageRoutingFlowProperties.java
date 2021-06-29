package com.obj.nc.flows.inputRouting.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = InputMessageRoutingFlowProperties.CONFIG_PROPS_PREFIX)
public class InputMessageRoutingFlowProperties {
		
	public enum RoutingType {
		FLOW_ID, PAYLOAD_TYPE
	}
	
	public static final String CONFIG_PROPS_PREFIX = "nc.flows.input-message-routing";

    private int pollPeriodInMilliSeconds = 1000;
    
    private RoutingType type = RoutingType.FLOW_ID;
    
    /*if type = PAYLOAD_TYPE. Map<son @type value, channel name>*/
    private Map<String,String> typeChannelMapping;
    
    private String typePropertyName = "@type";

}
