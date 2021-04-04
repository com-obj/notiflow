package com.obj.nc.flows.config;

import static com.obj.nc.flows.config.EmailProcessingFlowConfig.EMAIL_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.config.SmsProcessingFlowConfig.SMS_PROCESSING_FLOW_INPUT_CHANNEL_ID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromNotificationIntentProcessingFunction;

@Configuration
public class NotificationIntentProcessingFlowConfig {
		
	@Autowired private ValidateAndGenerateEventIdProcessingFunction generateEventId;
	@Autowired private MessagesFromNotificationIntentProcessingFunction generateMessagesFromEvent;
		
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
				.transform(generateEventId)
				.transform(generateMessagesFromEvent)
				.split()
				.routeToRecipients(spec -> spec.
						recipient(EMAIL_PROCESSING_FLOW_INPUT_CHANNEL_ID, m-> ((Message)m).isEmailMessage()).
						recipient(SMS_PROCESSING_FLOW_INPUT_CHANNEL_ID, m-> ((Message)m).isSmsMessage()).
						defaultOutputChannel("NON_EXTISTING_CHANNEL")
				)
				.get();
	}
	

}
