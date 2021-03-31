package com.obj.nc.flows.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

import com.obj.nc.functions.processors.messageTemplating.SmsTemplateFormatter;
import com.obj.nc.functions.processors.senders.SmsSender;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;
import com.obj.nc.services.SmsSenderExcecution;

@Configuration
@ConditionalOnBean(SmsSenderExcecution.class)
public class SmsProcessingFlowConfig {
	
	@Autowired private SmsSender smsSender;
	@Autowired private PaylaodLoggerSinkConsumer logConsumer;
	@Autowired private SmsTemplateFormatter smsFomratter;

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
				.transform(smsFomratter)
				.split()
				.transform(smsSender)
				.handle(logConsumer)
				.get();
	}
	
	@Bean
	public SmsSender smsSender(@Autowired SmsSenderExcecution<?> smsExecution) {
		return new SmsSender(smsExecution);
	}

}
