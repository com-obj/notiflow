package com.obj.nc.flows.inputEventRouting.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.router.AbstractMessageRouter;

import com.obj.nc.flows.inputEventRouting.InputEventRouter;
import com.obj.nc.functions.sources.genericEvents.GenericEventsSupplier;

@Configuration
public class InputEventRoutingFlowConfig {
		
	@Autowired private InputEventRoutingProperties routingProps;	
//	@Autowired private GenericEventsSupplier genericEventSupplier;	
	
    public static final String GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME = "genericEventSupplierFlowId"; 
    
    @Bean
    public IntegrationFlow inputEventRoutingFlow() {
    	return IntegrationFlows
			.fromSupplier(genericEventSupplier(), 
					conf-> conf.poller(Pollers.fixedRate(routingProps.getPollPeriodInMiliSeconds()))
					.id(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME))
			.channel(new DirectChannel())
			.route(inputEventRouter())
			.get();
    }

    @Bean
    public AbstractMessageRouter inputEventRouter() {
        return new InputEventRouter(); 
    }   
    
    @Bean
    public GenericEventsSupplier genericEventSupplier() {
    	return new GenericEventsSupplier();
    }

}
