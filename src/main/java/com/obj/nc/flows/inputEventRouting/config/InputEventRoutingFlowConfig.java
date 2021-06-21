package com.obj.nc.flows.inputEventRouting.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.router.AbstractMessageRouter;

import com.obj.nc.flows.inputEventRouting.FlowId2InputMessageRouter;
import com.obj.nc.flows.inputEventRouting.SimpleTypeBasedMessageRouter;
import com.obj.nc.functions.sources.genericEvents.GenericEventsSupplier;

@Configuration
public class InputEventRoutingFlowConfig {
	
	@Autowired private InputEventRoutingProperties routingProps;	
	@Autowired private GenericEventsSupplier genericEventSupplier;
	
    public static final String GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME = "genericEventSupplierFlowId"; 
//    public static final String GENERIC_EVENT_CHANNEL_ADAPTER_PAYLOAD_TYPE_BEAN_NAME = "genericEventSupplierPayloadType"; 
    
    @Bean
    @ConditionalOnProperty(value = "nc.flows.input-evet-routing.type", havingValue = "FLOW_ID")
    public IntegrationFlow flowIdBasedRoutingFlow() {
    	return IntegrationFlows
			.fromSupplier(genericEventSupplier, 
					conf-> conf.poller(Pollers.fixedRate(routingProps.getPollPeriodInMiliSeconds()))
					.id(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME))
			.route(flowIdRouter())
			.get();
    }

    @Bean
    @ConditionalOnProperty(value = "nc.flows.input-evet-routing.type", havingValue = "FLOW_ID")
    public AbstractMessageRouter flowIdRouter() {
        return new FlowId2InputMessageRouter();
    }
    
    @Bean
    @ConditionalOnProperty(value = "nc.flows.input-evet-routing.type", havingValue = "PAYLOAD_TYPE")
    public IntegrationFlow payloadTypeBasedRoutingFlow() {
    	return IntegrationFlows
			.fromSupplier(genericEventSupplier, 
					conf-> conf.poller(Pollers.fixedRate(routingProps.getPollPeriodInMiliSeconds()))
					.id(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME))
			.channel(new DirectChannel())
			.route(simplePayloadTypeBasedRouter())
			.get();
    }

    @Bean
    @ConditionalOnProperty(value = "nc.flows.input-evet-routing.type", havingValue = "PAYLOAD_TYPE")
    public AbstractMessageRouter simplePayloadTypeBasedRouter() {
        return new SimpleTypeBasedMessageRouter(); 
    }
    

    
    @Bean
    public GenericEventsSupplier genericEventSupplier() {
    	return new GenericEventsSupplier();
    }

}
