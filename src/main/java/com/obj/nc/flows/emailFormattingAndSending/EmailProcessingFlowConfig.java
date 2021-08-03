package com.obj.nc.flows.emailFormattingAndSending;

import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowProperties.MULTI_LOCALES_MERGE_STRATEGY.MERGE;

import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.functions.processors.messageTemplating.config.EmailTrackingConfigProperties;
import com.obj.nc.functions.processors.messageTracking.EmailReadTrackingDecorator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.obj.nc.domain.content.TemplateWithModelContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.endpointPersister.EndpointPersister;
import com.obj.nc.functions.processors.messageAggregator.aggregations.EmailMessageAggregationStrategy;
import com.obj.nc.functions.processors.messagePersister.MessagePersister;
import com.obj.nc.functions.processors.messageTemplating.EmailTemplateFormatter;
import com.obj.nc.functions.processors.senders.EmailSender;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class EmailProcessingFlowConfig {
	
	private final EmailSender emailSender;
	private final EmailTemplateFormatter emailFormatter;
	private final EmailReadTrackingDecorator emailReadTrackingDecorator;
	private final EmailTrackingConfigProperties emailTrackingConfigProperties;
	private final EmailProcessingFlowProperties properties;
	private final MessagePersister messagePersister;
	private final  EndpointPersister endpointPersister; 
	private final ThreadPoolTaskScheduler executor;
	private final EmailMessageAggregationStrategy emailMessageAggregationStrategy;
	
	public final static String EMAIL_FORMAT_AND_SEND_ROUTING_FLOW_ID = "EMAIL_FORMAT_AND_SEND_ROUTING_FLOW_ID";
	public final static String EMAIL_FORMAT_AND_SEND_ROUTING_FLOW_INPUT_CHANNEL_ID = EMAIL_FORMAT_AND_SEND_ROUTING_FLOW_ID + "_INPUT";
	
	public final static String EMAIL_FORMAT_FLOW_ID = "EMAIL_FORMAT_FLOW_ID";
	
	public final static String EMAIL_SEND_ROUTING_FLOW_ID = "EMAIL_SEND_ROUTING_FLOW_ID";
	public final static String EMAIL_SEND_ROUTING_FLOW_INPUT_CHANNEL_ID = EMAIL_SEND_ROUTING_FLOW_ID + "_INPUT";

	public final static String EMAIL_SEND_FLOW_ID = "EMAIL_SEND_FLOW_ID";
	public final static String EMAIL_SEND_FLOW_OUTPUT_CHANNEL_ID = EMAIL_SEND_FLOW_ID + "_OUTPUT";
	
	@Bean(EMAIL_FORMAT_AND_SEND_ROUTING_FLOW_ID)
	public IntegrationFlow emailFormatAndSendFlowDefinition() {
		return IntegrationFlows
				.from(emailFormatAndSendRoutingInputChannel())
				.routeToRecipients(spec -> spec
						.<Message<?>>recipient(
								emailFormatFlowDefinition().getInputChannel(),
								m -> m.getBody() instanceof TemplateWithModelContent)
						.defaultOutputChannel(emailSendRoutingInputChannel()))
				.get();
	}
	
	@Bean(EMAIL_FORMAT_FLOW_ID)
	public IntegrationFlow emailFormatFlowDefinition() {
		return flow -> flow
				.publishSubscribeChannel(executor, subscription  -> subscription
						//format email and merge if multilanguage
						.subscribe(aggregateMultilangFlow -> aggregateMultilangFlow
								.filter(m-> MERGE.equals(properties.getMultiLocalesMergeStrategy()))
								.handle(emailFormatter)
								.handle(emailMessageAggregationStrategy)
								.channel(emailSendRoutingInputChannel()))
						//format and split if multilanguage
						.subscribe(mesagePerLocaleFlow -> mesagePerLocaleFlow
								.filter(m-> !MERGE.equals(properties.getMultiLocalesMergeStrategy()))
								.split(emailFormatter)
								.channel(emailSendRoutingInputChannel()))
				);
	}
	
	@Bean(EMAIL_SEND_ROUTING_FLOW_ID)
	public IntegrationFlow emailSendRoutingFlowDefinition() {
		return IntegrationFlows
				.from(emailSendRoutingInputChannel())
				.routeToRecipients(spec -> spec
						.recipientFlow((Message<EmailContent> source) -> emailTrackingConfigProperties.isEnabled() 
										&& MediaType.TEXT_HTML_VALUE.equals(source.getBody().getContentType()),
								trackingSubflow -> trackingSubflow
										.handle(emailReadTrackingDecorator)
										.channel(emailSendFlowDefinition().getInputChannel()))
						.defaultOutputChannel(emailSendFlowDefinition().getInputChannel()))
				.get();
	}
	
	@Bean(EMAIL_SEND_FLOW_ID)
	public IntegrationFlow emailSendFlowDefinition() {
		return flow -> flow
				.handle(endpointPersister)
				.handle(messagePersister)
				.handle(emailSender)
				.wireTap(flowConfig -> flowConfig.channel(DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID))
				.channel(emailSendOutputChannel());
	}
	
	@Bean(EMAIL_FORMAT_AND_SEND_ROUTING_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel emailFormatAndSendRoutingInputChannel() {
		return new PublishSubscribeChannel(executor);
	}
	
	@Bean(EMAIL_SEND_ROUTING_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel emailSendRoutingInputChannel() {
		return new PublishSubscribeChannel(executor);
	}
	
	@Bean(EMAIL_SEND_FLOW_OUTPUT_CHANNEL_ID)
	public MessageChannel emailSendOutputChannel() {
		return new PublishSubscribeChannel(executor);
	}
	
}
