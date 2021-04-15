package com.obj.nc.flows.intenToMessageToSender;

import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowConfig.EMAIL_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.smsFormattingAndSending.SmsProcessingFlowConfig.SMS_PROCESSING_FLOW_INPUT_CHANNEL_ID;

import com.obj.nc.functions.processors.senders.SmsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.deliveryInfo.DeliveryInfoProcessingGenerator;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromNotificationIntentProcessingFunction;
import com.obj.nc.functions.sink.deliveryInfoPersister.DeliveryInfoPersister;

@Configuration
@ConditionalOnBean(SmsSender.class) // ked nie je definovany SmsSender, Spring nemoze vytvorit flow SMS_PROCESSING_FLOW_ID a teda neda sa routovat do SMS_PROCESSING_FLOW_INPUT_CHANNEL_ID
public class NotificationIntentProcessingFlowConfig {
		
//	@Autowired private GenerateEventIdProcessingFunction generateEventId;
	@Autowired private MessagesFromNotificationIntentProcessingFunction generateMessagesFromEvent;
	@Autowired private DeliveryInfoProcessingGenerator deliveryInfoProcessingGenerator;
	@Autowired private DeliveryInfoPersister deliveryPersister;
		
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
				.transform(generateMessagesFromEvent)
				.split()
				.wireTap( flowConfig -> 
					flowConfig
					.handle(deliveryInfoProcessingGenerator)
					.split()
					.handle(deliveryPersister)
				)
				.routeToRecipients(spec -> spec.
						recipient(EMAIL_PROCESSING_FLOW_INPUT_CHANNEL_ID, m-> ((Message)m).isEmailMessage()).
						recipient(SMS_PROCESSING_FLOW_INPUT_CHANNEL_ID, m-> ((Message)m).isSmsMessage()).
						defaultOutputChannel("NON_EXTISTING_CHANNEL")
				)
				.get();
	}
	

}
