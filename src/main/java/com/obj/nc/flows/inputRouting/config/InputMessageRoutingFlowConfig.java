package com.obj.nc.flows.inputRouting.config;

import com.obj.nc.flows.inputRouting.FlowId2InputMessageRouter;
import com.obj.nc.flows.inputRouting.SimpleTypeBasedMessageRouter;
import com.obj.nc.functions.sources.message.MessageSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;

@Configuration
public class InputMessageRoutingFlowConfig {
	
	@Autowired private InputMessageRoutingFlowProperties routingProps;	
	@Autowired private MessageSupplier messageSupplier;
    @Autowired private FlowId2InputMessageRouter flowIdRouter;
    @Autowired private SimpleTypeBasedMessageRouter simplePayloadTypeBasedRouter;
	
    public static final String MESSAGE_CHANNEL_ADAPTER_BEAN_NAME = "messageSupplierFlowId"; 
    
    @Bean
    @ConditionalOnProperty(value = "nc.flows.input-message-routing.type", havingValue = "FLOW_ID")
    public IntegrationFlow flowIdBasedMessageRoutingFlow() {
    	return IntegrationFlows
			.fromSupplier(messageSupplier, 
					conf-> conf.poller(Pollers.fixedRate(routingProps.getPollPeriodInMilliSeconds()))
					.id(MESSAGE_CHANNEL_ADAPTER_BEAN_NAME))
			.route(flowIdRouter)
			.get();
    }

    @Bean
    @ConditionalOnProperty(value = "nc.flows.input-message-routing.type", havingValue = "PAYLOAD_TYPE")
    public IntegrationFlow payloadTypeBasedMessageRoutingFlow() {
    	return IntegrationFlows
			.fromSupplier(messageSupplier, 
					conf-> conf.poller(Pollers.fixedRate(routingProps.getPollPeriodInMilliSeconds()))
					.id(MESSAGE_CHANNEL_ADAPTER_BEAN_NAME))
			.channel(new DirectChannel())
			.route(simplePayloadTypeBasedRouter)
			.get();
    }

    @Bean
    public MessageSupplier messageSupplier() {
    	return new MessageSupplier();
    }

}
