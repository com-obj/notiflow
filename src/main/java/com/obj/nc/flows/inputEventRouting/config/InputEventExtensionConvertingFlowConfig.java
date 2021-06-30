package com.obj.nc.flows.inputEventRouting.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

import com.obj.nc.flows.inputEventRouting.extensions.EventProcessorExtension;
import com.obj.nc.flows.messageProcessing.MessageProcessingFlowConfig;
import com.obj.nc.functions.processors.event2Message.Event2MessageExtensionsConverter;

import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class InputEventExtensionConvertingFlowConfig {
	
	@Autowired 
	private Event2MessageExtensionsConverter eventConvertingExtension;
	
	@SuppressWarnings("unused")
	@Autowired(required = false)
	private List<EventProcessorExtension<?>> eventProcessors = new ArrayList<>();
	
	public final static String EVENT_CONVERTING_EXTENSION_FLOW_ID = "EVENT_CONVERTING_EXTENSION_FLOW_ID";
	public final static String EVENT_CONVERTING_EXTENSION_FLOW_ID_INPUT_CHANNEL_ID = EVENT_CONVERTING_EXTENSION_FLOW_ID + "_INPUT";
				
    public static final String GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME = "genericEventSupplierFlowId"; 
    
	@Bean(EVENT_CONVERTING_EXTENSION_FLOW_ID_INPUT_CHANNEL_ID)
	public PublishSubscribeChannel messageProcessingInputChannel() {
		return new PublishSubscribeChannel();
	}
    
    @Bean
    public IntegrationFlow extensionBasedRoutingFlow() {
    	return IntegrationFlows
			.from(messageProcessingInputChannel())
			.handle(eventConvertingExtension)
			.split()
			.channel(MessageProcessingFlowConfig.MESSAGE_PROCESSING_FLOW_INPUT_CHANNEL_ID)
			.get();
    }

}
