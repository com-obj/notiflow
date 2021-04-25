package com.obj.nc.flows.intenToMessageToSender;

import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.DELIVERY_INFO_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowConfig.EMAIL_FROMAT_AND_SEND_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.smsFormattingAndSending.SmsProcessingFlowConfig.SMS_PROCESSING_FLOW_INPUT_CHANNEL_ID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromNotificationIntentProcessingFunction;
import com.obj.nc.functions.sink.deliveryInfoPersister.domain.DeliveryInfo;
import com.obj.nc.functions.sink.intentPersister.NotificationIntentPersister;

@Configuration
public class NotificationIntentProcessingFlowConfig {
		
	@Autowired private MessagesFromNotificationIntentProcessingFunction generateMessagesFromIntent;
	@Autowired private NotificationIntentPersister notificationIntentPersister;
	
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
				.wireTap( flowConfig->
					flowConfig.handle(notificationIntentPersister))
				.transform(generateMessagesFromIntent)
				.split()
				.wireTap( flowConfig -> 
					flowConfig.channel(DELIVERY_INFO_PROCESSING_FLOW_INPUT_CHANNEL_ID)
				)
				.routeToRecipients(spec -> spec.
						recipient(EMAIL_FROMAT_AND_SEND_INPUT_CHANNEL_ID, m-> ((Message)m).isEmailMessage()).
						recipient(SMS_PROCESSING_FLOW_INPUT_CHANNEL_ID, m-> ((Message)m).isSmsMessage()).
						defaultOutputChannel("NON_EXTISTING_CHANNEL")
				)
				.get();
	}
	

}
