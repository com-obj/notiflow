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
	public final static String MULTI_LOCALES_AGGREGATION_FLOW_ID = "MULTI_LOCALES_AGGREGATION_FLOW_ID";
	public final static String MULTI_LOCALES_AGGREGATION_INPUT_CHANNEL_ID = MULTI_LOCALES_AGGREGATION_FLOW_ID + "_INPUT";
	public final static String EMAIL_SENDING_INPUT_CHANNEL_ID = "EMAIL_SENDING_INPUT";

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
						.recipient(multiLocalesAggregationInputChannel(), 
								m -> EmailProcessingFlowProperties.MULTI_LOCALES_MERGE_STRATEGY.MERGE.equals(properties.getMultiLocalesMergeStrategy()))
						.defaultOutputToParentFlow()
				)
				.channel(emailSendingInputChannel())
				.split()
				.handle(emailSender)
				.wireTap( flowConfig -> 
					flowConfig.channel(DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID)
				)
				.handle(logConsumer)
				.get();
	}
	
	@Bean(MULTI_LOCALES_AGGREGATION_FLOW_ID)
	public IntegrationFlow emailAggregatingFlowDefinition() {
		return IntegrationFlows
				.from(multiLocalesAggregationInputChannel())
				.handle(messageAggregator())
				.channel(emailSendingInputChannel())
				.get();
	}
	
	@Bean(MULTI_LOCALES_AGGREGATION_INPUT_CHANNEL_ID)
	public MessageChannel multiLocalesAggregationInputChannel() {
		return new PublishSubscribeChannel();
	}
	
	@Bean(EMAIL_SENDING_INPUT_CHANNEL_ID)
	public MessageChannel emailSendingInputChannel() {
		return new PublishSubscribeChannel();
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
