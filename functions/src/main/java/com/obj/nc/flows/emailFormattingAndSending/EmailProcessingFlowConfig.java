package com.obj.nc.flows.emailFormattingAndSending;

import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.messageAggregator.MessageAggregator;
import com.obj.nc.functions.processors.messageAggregator.aggregations.EmailMessageAggregationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

import com.obj.nc.functions.processors.messageTemplating.EmailTemplateFormatter;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;

@Configuration
public class EmailProcessingFlowConfig {
	
	@Autowired private EmailSender emailSender;
	@Autowired private PaylaodLoggerSinkConsumer logConsumer;
	@Autowired private EmailTemplateFormatter emailFormatter;
	@Autowired private EmailProcessingFlowProperties properties;
	
	public final static String EMAIL_PROCESSING_FLOW_ID = "EMAIL_PROCESSING_FLOW_ID";
	public final static String EMAIL_PROCESSING_FLOW_INPUT_CHANNEL_ID = EMAIL_PROCESSING_FLOW_ID + "_INPUT";

	
	public final static String DELIVERY_INFO_INPUT_CHANNEL_ID = "DELIVERY_INFO_INPUT";
	
	public final static String EMAIL_PROCESSING_FLOW_AGGREGATION_STRATEGY = "EMAIL_PROCESSING_FLOW_AGGREGATION_STRATEGY";
	public final static String EMAIL_PROCESSING_FLOW_MESSAGE_AGGREGATOR = "EMAIL_PROCESSING_FLOW_MESSAGE_AGGREGATOR";

	@Bean(EMAIL_PROCESSING_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel emailProcessingInputChangel() {
		return new PublishSubscribeChannel();
	}
	
	@Bean(EMAIL_PROCESSING_FLOW_ID)
	public IntegrationFlow emailProcessingFlowDefinition() {
		return IntegrationFlows
				.from(emailProcessingInputChangel())
				.handle(emailFormatter)
				.routeToRecipients(spec -> spec
						.recipient("aggregationChannel", 
								m -> EmailProcessingFlowProperties.MULTI_LOCALES_MERGE_STRATEGY.MERGE.equals(properties.getMultiLocalesMergeStrategy()))
						.defaultOutputChannel("emailSendingChannel")
				)
				.channel("emailSendingChannel")
				.split()
				.handle(emailSender)
				.wireTap( flowConfig -> 
					flowConfig.channel(DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID)
				)
				.handle(logConsumer)
				.get();
	}
	
	@Bean("emailAggregatingFlowId")
	public IntegrationFlow emailAggregatingFlowDefinition() {
		return IntegrationFlows
				.from("aggregationChannel")
				.handle(messageAggregator())
				.channel("emailSendingChannel")
				.get();
	}
	
	@Bean(EMAIL_PROCESSING_FLOW_AGGREGATION_STRATEGY)
	public EmailMessageAggregationStrategy emailMessageAggregationStrategy() {
		return new EmailMessageAggregationStrategy();
	}
	
	@Bean(EMAIL_PROCESSING_FLOW_MESSAGE_AGGREGATOR)
	public MessageAggregator messageAggregator() {
		return new MessageAggregator(emailMessageAggregationStrategy());
	}

}
