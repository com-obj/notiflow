package com.obj.nc.flows.emailFormattingAndSending;

import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.messageAggregator.MessageAggregator;
import com.obj.nc.domain.content.TemplateWithModelContent;
import com.obj.nc.domain.message.Message;
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

import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowProperties.MULTI_LOCALES_MERGE_STRATEGY.MERGE;

@Configuration
public class EmailProcessingFlowConfig {
	
	@Autowired private EmailSender emailSender;
	@Autowired private PaylaodLoggerSinkConsumer logConsumer;
	@Autowired private EmailTemplateFormatter emailFormatter;
	@Autowired private EmailProcessingFlowProperties properties;
	
	public final static String EMAIL_PROCESSING_FLOW_ID = "EMAIL_PROCESSING_FLOW_ID";
	public final static String EMAIL_PROCESSING_FLOW_INPUT_CHANNEL_ID = EMAIL_PROCESSING_FLOW_ID + "_INPUT";

	
	public final static String DELIVERY_INFO_INPUT_CHANNEL_ID = "DELIVERY_INFO_INPUT";
	
	public final static String MULTI_LOCALES_AGGREGATION_FLOW_ID = "MULTI_LOCALES_AGGREGATION_FLOW_ID";
	public final static String MULTI_LOCALES_AGGREGATION_STRATEGY = "MULTI_LOCALES_AGGREGATION_STRATEGY";
	public final static String MULTI_LOCALES_AGGREGATION_INPUT_CHANNEL_ID = MULTI_LOCALES_AGGREGATION_FLOW_ID + "_INPUT";
	
	public final static String EMAIL_FORMATING_FLOW_ID = "EMAIL_FORMATING_FLOW_ID";
	public final static String EMAIL_FORMATING_INPUT_CHANNEL_ID = EMAIL_FORMATING_FLOW_ID + "_INPUT";
	public final static String EMAIL_FORMATING_OUTPUT_CHANNEL_ID = EMAIL_FORMATING_FLOW_ID + "_OUTPUT";
	
	public final static String EMAIL_SENDING_INPUT_CHANNEL_ID = "EMAIL_SENDING_INPUT";

	@Bean(EMAIL_PROCESSING_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel emailProcessingInputChangel() {
		return new PublishSubscribeChannel();
	}
	
	@Bean(EMAIL_PROCESSING_FLOW_ID)
	public IntegrationFlow emailProcessingFlowDefinition() {
		return IntegrationFlows
				.from(emailProcessingInputChangel())
				.routeToRecipients(spec -> spec
						.recipient(EMAIL_FORMATING_INPUT_CHANNEL_ID,
								m -> ((Message) m).getBody().getMessage() instanceof TemplateWithModelContent)
						.defaultOutputToParentFlow()
				)
				.publishSubscribeChannel(c -> c.id(EMAIL_FORMATING_OUTPUT_CHANNEL_ID))
				.routeToRecipients(spec -> spec
						.recipient(MULTI_LOCALES_AGGREGATION_INPUT_CHANNEL_ID,
								m -> MERGE.equals(properties.getMultiLocalesMergeStrategy()))
						.defaultOutputToParentFlow()
				)
				.split()
				.publishSubscribeChannel(c -> c.id(EMAIL_SENDING_INPUT_CHANNEL_ID))
				.handle(emailSender)
				.wireTap( flowConfig -> 
					flowConfig.channel(DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID)
				)
				.handle(logConsumer)
				.get();
	}
	
	@Bean(EMAIL_FORMATING_FLOW_ID)
	public IntegrationFlow emailFormatingFlowDefinition() {
		return IntegrationFlows
				.from(EMAIL_FORMATING_INPUT_CHANNEL_ID)
				.handle(emailFormatter)
				.channel(EMAIL_FORMATING_OUTPUT_CHANNEL_ID)
				.get();
	}
	
	@Bean(MULTI_LOCALES_AGGREGATION_FLOW_ID)
	public IntegrationFlow multiLocalesAggregationFlowDefinition() {
		return IntegrationFlows
				.from(MULTI_LOCALES_AGGREGATION_INPUT_CHANNEL_ID)
				.handle(multiLocalesAggregationStrategy())
				.channel(EMAIL_SENDING_INPUT_CHANNEL_ID)
				.get();
	}
	
	@Bean(MULTI_LOCALES_AGGREGATION_STRATEGY)
	public EmailMessageAggregationStrategy multiLocalesAggregationStrategy() {
		return new EmailMessageAggregationStrategy();
	}
	
	@Bean(EMAIL_FORMATING_INPUT_CHANNEL_ID)
	public MessageChannel emailFormatingInputChannel() {
		return new PublishSubscribeChannel();
	}
	
	@Bean(MULTI_LOCALES_AGGREGATION_INPUT_CHANNEL_ID)
	public MessageChannel multiLocalesAggregationInputChannel() {
		return new PublishSubscribeChannel();
	}

}
