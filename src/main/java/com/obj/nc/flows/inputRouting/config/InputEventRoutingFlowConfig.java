package com.obj.nc.flows.inputRouting.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;

import com.obj.nc.flows.inputRouting.FlowId2InputMessageRouter;
import com.obj.nc.flows.inputRouting.SimpleTypeBasedMessageRouter;
import com.obj.nc.functions.sources.genericEvents.GenericEventsSupplier;

@Configuration
public class InputEventRoutingFlowConfig {
	
	@Autowired private InputEventRoutingProperties routingProps;	
	@Autowired private GenericEventsSupplier genericEventSupplier;
    @Autowired private FlowId2InputMessageRouter flowIdRouter;
    @Autowired private SimpleTypeBasedMessageRouter simplePayloadTypeBasedRouter;
	
    public static final String GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME = "genericEventSupplierFlowId"; 
    
    @Bean
    @ConditionalOnProperty(value = "nc.flows.input-evet-routing.type", havingValue = "FLOW_ID")
    public IntegrationFlow flowIdBasedEventRoutingFlow() {
    	return IntegrationFlows
			.fromSupplier(genericEventSupplier, 
					conf-> conf.poller(Pollers.fixedRate(routingProps.getPollPeriodInMiliSeconds()))
					.id(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME))
			.route(flowIdRouter)
			.get();
    }

    @Bean
    @ConditionalOnProperty(value = "nc.flows.input-evet-routing.type", havingValue = "PAYLOAD_TYPE")
    public IntegrationFlow payloadTypeBasedEventRoutingFlow() {
    	return IntegrationFlows
			.fromSupplier(genericEventSupplier, 
					conf-> conf.poller(Pollers.fixedRate(routingProps.getPollPeriodInMiliSeconds()))
					.id(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME))
			.channel(new DirectChannel())
			.route(simplePayloadTypeBasedRouter)
			.get();
    }

    @Bean
    public GenericEventsSupplier genericEventSupplier() {
    	return new GenericEventsSupplier();
    }

}
