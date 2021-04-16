package com.obj.nc.flows.emailFormattingAndSending;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

import com.obj.nc.functions.processors.deliveryInfo.DeliveryInfoDeliveredGenerator;
import com.obj.nc.functions.processors.messageTemplating.EmailTemplateFormatter;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.functions.sink.deliveryInfoPersister.DeliveryInfoPersister;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;

@Configuration
public class EmailProcessingFlowConfig {
	
	@Autowired private EmailSender emailSender;
	@Autowired private PaylaodLoggerSinkConsumer logConsumer;
	@Autowired private EmailTemplateFormatter emailFormatter;
	@Autowired private DeliveryInfoPersister deliveryPersister;
	@Autowired private DeliveryInfoDeliveredGenerator deliveryInfoGenerator;
	
	public final static String EMAIL_PROCESSING_FLOW_ID = "EMAIL_PROCESSING_FLOW_ID";
	public final static String EMAIL_PROCESSING_FLOW_INPUT_CHANNEL_ID = EMAIL_PROCESSING_FLOW_ID + "_INPUT";
	
	public final static String DELIVERY_INFO_INPUT_CHANNEL_ID = "DELIVERY_INFO_INPUT";

	@Bean(EMAIL_PROCESSING_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel emailProcessingInputChangel() {
		return new PublishSubscribeChannel();
	}
	
	@Bean(EMAIL_PROCESSING_FLOW_ID)
	public IntegrationFlow emailProcessingFlowDefinition() {
		return IntegrationFlows
				.from(emailProcessingInputChangel())
				.handle(emailFormatter)
				.split()
				.handle(emailSender)
				.wireTap( flowConfig -> 
					flowConfig
					.handle(deliveryInfoGenerator)
					.split()
					.handle(deliveryPersister)
				)
//				.handle(deliveryInfoGenerator)
//				.split()
//				.publishSubscribeChannel(consDef -> consDef.id(DELIVERY_INFO_INPUT_CHANNEL_ID))
//				.handle(deliveryPersister)
				.handle(logConsumer)
				.get();
	}


}
