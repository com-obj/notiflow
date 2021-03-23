package com.obj.nc.flows.inputEventRouting.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.router.AbstractMessageRouter;

import com.obj.nc.flows.inputEventRouting.FlowId2InputMessageRouter;
import com.obj.nc.functions.sources.genericEvents.GenericEventsForProcessingSupplier;

@Configuration
@ConditionalOnProperty(value = "nc.flows.input-evet-routing.enabled", havingValue = "true")
public class InputEventRoutingFlowConfig {
	
	@Autowired private InputEventRoutingProperties routingProps;	
	@Autowired private GenericEventsForProcessingSupplier genericEventSupplier;
    
    @Bean
    public IntegrationFlow flowIdBasedRoutingFlow() {
    	return IntegrationFlows
			.from(genericEventSupplier, 
					conf-> conf.poller(Pollers.fixedRate(routingProps.getPollPeriodInMiliSeconds())))
			.route(flowIdRouter())
			.get();
    }

    @Bean
    public AbstractMessageRouter flowIdRouter() {
        return new FlowId2InputMessageRouter();
    }
    

}
