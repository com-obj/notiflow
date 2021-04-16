package com.obj.nc.flows.smsFormattingAndSending;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

import com.obj.nc.functions.processors.deliveryInfo.DeliveryInfoDeliveredGenerator;
import com.obj.nc.functions.processors.messageTemplating.SmsTemplateFormatter;
import com.obj.nc.functions.processors.senders.SmsSender;
import com.obj.nc.functions.sink.deliveryInfoPersister.DeliveryInfoPersister;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;

@Configuration
@ConditionalOnBean(SmsSender.class)
public class SmsProcessingFlowConfig {
	
	@Autowired private SmsSender smsSender;
	@Autowired private SmsTemplateFormatter smsFomratter;
	@Autowired private PaylaodLoggerSinkConsumer logConsumer;
	@Autowired private DeliveryInfoPersister deliveryPersister;
	@Autowired private DeliveryInfoDeliveredGenerator deliveryInfoGenerator;

	public final static String SMS_PROCESSING_FLOW_ID = "SMS_PROCESSING_FLOW_ID";
	public final static String SMS_PROCESSING_FLOW_INPUT_CHANNEL_ID = SMS_PROCESSING_FLOW_ID + "_INPUT";
	
	//TODO, should be one with EMAIL
	public final static String DELIVERY_INFO_INPUT_CHANNEL_ID = "DELIVERY_INFO_SMS_INPUT";
	
	@Bean(SMS_PROCESSING_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel smsProcessingInputChangel() {
		return new PublishSubscribeChannel();
	}
	
	@Bean(SMS_PROCESSING_FLOW_ID)
	public IntegrationFlow smsProcessingFlowDefinition() {
		return IntegrationFlows
				.from(smsProcessingInputChangel())
				.handle(smsFomratter)
				.split()
				.handle(smsSender)
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
