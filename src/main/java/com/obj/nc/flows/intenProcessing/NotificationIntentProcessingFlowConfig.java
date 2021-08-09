package com.obj.nc.flows.intenProcessing;

import static com.obj.nc.flows.messageProcessing.MessageProcessingFlowConfig.MESSAGE_PROCESSING_FLOW_INPUT_CHANNEL_ID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

import com.obj.nc.functions.processors.endpointPersister.EndpointPersister;
import com.obj.nc.functions.processors.intentPersister.NotificationIntentPersister;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromIntentGenerator;

import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class NotificationIntentProcessingFlowConfig {
		
	@Autowired private MessagesFromIntentGenerator generateMessagesFromIntent;
	@Autowired private NotificationIntentPersister notificationIntentPersister;
	@Autowired private EndpointPersister endpointPersister; 
	
	public final static String INTENT_PROCESSING_FLOW_ID = "INTENT_PROCESSING_FLOW_ID";
	public final static String INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID = INTENT_PROCESSING_FLOW_ID + "_INPUT";
	
	@Bean(INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel intentProcessingInputChangel() {
		return new PublishSubscribeChannel();
	}
	
	@Bean(INTENT_PROCESSING_FLOW_ID)
	public IntegrationFlow intentProcessingFlowDefinition() {
		return IntegrationFlows
				.from(intentProcessingInputChangel())
				.handle(endpointPersister)
				.handle(notificationIntentPersister)				
				.transform(generateMessagesFromIntent)
				.split()
				.channel(MESSAGE_PROCESSING_FLOW_INPUT_CHANNEL_ID)
				.get();
	}
	
}
