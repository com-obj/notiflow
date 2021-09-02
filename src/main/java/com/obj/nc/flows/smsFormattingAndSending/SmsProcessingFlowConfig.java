package com.obj.nc.flows.smsFormattingAndSending;

import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

import com.obj.nc.functions.processors.endpointPersister.EndpointPersister;
import com.obj.nc.functions.processors.messagePersister.MessagePersister;
import com.obj.nc.functions.processors.messageTemplating.SmsTemplateFormatter;
import com.obj.nc.functions.processors.senders.SmsSender;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SmsProcessingFlowConfig {
	
	private final SmsSender smsSender;
	private final SmsTemplateFormatter smsFormatter;
	private final PaylaodLoggerSinkConsumer logConsumer;
	private final MessagePersister messagePersister;
	private final EndpointPersister endpointPersister;

	public final static String SMS_PROCESSING_FLOW_ID = "SMS_PROCESSING_FLOW_ID";
	public final static String SMS_PROCESSING_FLOW_INPUT_CHANNEL_ID = SMS_PROCESSING_FLOW_ID + "_INPUT";
	
	@Bean(SMS_PROCESSING_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel smsProcessingInputChangel() {
		return new PublishSubscribeChannel();
	}
	
	@Bean(SMS_PROCESSING_FLOW_ID)
	public IntegrationFlow smsProcessingFlowDefinition() {
		return IntegrationFlows
				.from(smsProcessingInputChangel())
				.handle(endpointPersister)
				.handle(messagePersister)
				.handle(smsFormatter)
				.split()
				.handle(messagePersister)
				.handle(smsSender)
				.wireTap( flowConfig -> 
					flowConfig.channel(DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID)
				)
				.handle(logConsumer)
				.get();

	}

}
