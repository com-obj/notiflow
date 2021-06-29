package com.obj.nc.flows.inputRouting.config;

import com.obj.nc.flows.inputRouting.FlowId2InputMessageRouter;
import com.obj.nc.flows.inputRouting.SimpleTypeBasedMessageRouter;
import com.obj.nc.functions.sources.intent.NotificationIntentSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;

@Configuration
public class InputIntentRoutingFlowConfig {
	
	@Autowired private InputMessageRoutingFlowProperties routingProps;	
	@Autowired private NotificationIntentSupplier notificationIntentSupplier;
    @Autowired private FlowId2InputMessageRouter flowIdRouter;
    @Autowired private SimpleTypeBasedMessageRouter simplePayloadTypeBasedRouter;
	
    public static final String MESSAGE_CHANNEL_ADAPTER_BEAN_NAME = "messageSupplierFlowId"; 
    
    @Bean
    @ConditionalOnProperty(value = "nc.flows.input-intent-routing.type", havingValue = "FLOW_ID")
    public IntegrationFlow flowIdBasedMessageRoutingFlow() {
    	return IntegrationFlows
			.fromSupplier(notificationIntentSupplier, 
					conf-> conf.poller(Pollers.fixedRate(routingProps.getPollPeriodInMilliSeconds()))
					.id(MESSAGE_CHANNEL_ADAPTER_BEAN_NAME))
			.route(flowIdRouter)
			.get();
    }

    @Bean
    @ConditionalOnProperty(value = "nc.flows.input-intent-routing.type", havingValue = "PAYLOAD_TYPE")
    public IntegrationFlow payloadTypeBasedMessageRoutingFlow() {
    	return IntegrationFlows
			.fromSupplier(notificationIntentSupplier, 
					conf-> conf.poller(Pollers.fixedRate(routingProps.getPollPeriodInMilliSeconds()))
					.id(MESSAGE_CHANNEL_ADAPTER_BEAN_NAME))
			.channel(new DirectChannel())
			.route(simplePayloadTypeBasedRouter)
			.get();
    }

    @Bean
    public NotificationIntentSupplier notificationIntentSupplier() {
    	return new NotificationIntentSupplier();
    }

}
