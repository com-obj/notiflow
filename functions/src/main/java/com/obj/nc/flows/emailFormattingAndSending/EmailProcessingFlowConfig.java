package com.obj.nc.flows.emailFormattingAndSending;

import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowProperties.MULTI_LOCALES_MERGE_STRATEGY.MERGE;

import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.functions.processors.messageTemplating.config.EmailTrackingConfigProperties;
import com.obj.nc.functions.processors.messageTracking.EmailReadTrackingDecorator;
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
import com.obj.nc.functions.processors.messageAggregator.aggregations.EmailMessageAggregationStrategy;
import com.obj.nc.functions.processors.messageTemplating.EmailTemplateFormatter;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.functions.sink.messagePersister.MessagePersister;

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
	private final ThreadPoolTaskScheduler executor;
	
	public final static String EMAIL_FORMAT_AND_SEND_FLOW_ID = "EMAIL_FORMAT_AND_SEND_FLOW_ID";
	public final static String EMAIL_FROMAT_AND_SEND_INPUT_CHANNEL_ID = EMAIL_FORMAT_AND_SEND_FLOW_ID + "_INPUT";

	public final static String EMAIL_SENDING_FLOW_ID = "EMAIL_SENDING_FLOW_ID";
	public final static String EMAIL_SENDING_FLOW_INPUT_CHANNEL_ID = EMAIL_SENDING_FLOW_ID + "_INPUT";
	public final static String EMAIL_SENDING_FLOW_OUTPUT_CHANNEL_ID = EMAIL_SENDING_FLOW_ID + "_OUTPUT";
	
	public final static String EMAIL_FORMATING_FLOW_ID = "EMAIL_FORMATING_FLOW_ID";
	public final static String EMAIL_FORMATING_INPUT_CHANNEL_ID = EMAIL_FORMATING_FLOW_ID + "_INPUT";
	
	public final static String EMAIL_TRACKING_DECORATION_FLOW_ID = "EMAIL_TRACKING_DECORATION_FLOW_ID";
	public final static String EMAIL_TRACKING_DECORATION_INPUT_CHANNEL_ID = EMAIL_TRACKING_DECORATION_FLOW_ID + "_INPUT";

	@Bean(EMAIL_FROMAT_AND_SEND_INPUT_CHANNEL_ID)
	public MessageChannel emailProcessingInputChangel() {
		return new PublishSubscribeChannel(executor);
	}
	
	@Bean(EMAIL_SENDING_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel emailSedningInputChangel() {
		return new PublishSubscribeChannel(executor);
	}
	
	@Bean(EMAIL_SENDING_FLOW_OUTPUT_CHANNEL_ID)
	public MessageChannel emailSedningOutputChangel() {
		return new PublishSubscribeChannel();
	}
	
//	@Bean(EMAIL_FORMATING_INPUT_CHANNEL_ID)
//	public MessageChannel emailFormatingInputChangel() {
//		return new PublishSubscribeChannel(executor);
//	}
	
	@Bean(EMAIL_FORMAT_AND_SEND_FLOW_ID)
	public IntegrationFlow emailProcessingFlowDefinition() {
		return IntegrationFlows
				.from(emailProcessingInputChangel())
				.routeToRecipients(spec -> spec
						.recipient(
								EMAIL_FORMATING_INPUT_CHANNEL_ID,
								m -> 
									((Message<?>) m).getBody() instanceof TemplateWithModelContent
								)
						.recipient(
								EMAIL_TRACKING_DECORATION_INPUT_CHANNEL_ID,
								m -> 
									((Message<?>) m).getBody() instanceof EmailContent 
								)
						.defaultOutputChannel(
								EMAIL_SENDING_FLOW_INPUT_CHANNEL_ID)
				)
				.get();
	}
	
	@Bean(EMAIL_SENDING_FLOW_ID)
	public IntegrationFlow emaiSendingFlowDefinition() {
		return IntegrationFlows
				.from(emailSedningInputChangel())
				.wireTap( flowConfig -> 
					flowConfig.handle(messagePersister)
				)
				.handle(emailSender)
				.wireTap( flowConfig -> 
					flowConfig.channel(DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID)
				)
				.channel(emailSedningOutputChangel())
				.get();
	}
	
	@Bean(EMAIL_FORMATING_FLOW_ID)
	public IntegrationFlow emailFormatingFlowDefinition() {
		return flow -> flow
			.publishSubscribeChannel(executor, subscription  ->
				subscription.id(EMAIL_FORMATING_INPUT_CHANNEL_ID)
					//format email and merge if multilanguage
					.subscribe(aggregateMultilangFlow -> aggregateMultilangFlow
							.filter(m-> MERGE.equals(properties.getMultiLocalesMergeStrategy()))
							.handle(emailFormatter)
							.handle(multiLocalesAggregationStrategy())
							.routeToRecipients(r -> r.recipient(EMAIL_TRACKING_DECORATION_INPUT_CHANNEL_ID)))
					//format and split if multilanguage
					.subscribe(mesagePerLocaleFlow -> mesagePerLocaleFlow
							.filter(m-> !MERGE.equals(properties.getMultiLocalesMergeStrategy()))
							.split(emailFormatter)
							.routeToRecipients(r -> r.recipient(EMAIL_TRACKING_DECORATION_INPUT_CHANNEL_ID)))	
				);

	}
	
	@Bean(EMAIL_TRACKING_DECORATION_FLOW_ID)
	public IntegrationFlow emaiTrackingDecorationFlowDefinition() {
		return flow -> flow
			.publishSubscribeChannel(executor, subscription -> subscription.id(EMAIL_TRACKING_DECORATION_INPUT_CHANNEL_ID)

				.subscribe(decoratingFlow -> decoratingFlow
						.filter(m -> {
							MessageContent msgBody = ((Message<?>) m).getBody();
							return emailTrackingConfigProperties.isEnabled() 
									&& msgBody instanceof EmailContent 
									&& MediaType.TEXT_HTML_VALUE.equals(((EmailContent) msgBody).getContentType());
						})
						.handle(emailReadTrackingDecorator)
						.channel(EMAIL_SENDING_FLOW_INPUT_CHANNEL_ID))

				.subscribe(skipDecoratingFlow -> skipDecoratingFlow
						.filter(m -> {
							MessageContent msgBody = ((Message<?>) m).getBody();
							return !emailTrackingConfigProperties.isEnabled()
									|| !(msgBody instanceof EmailContent)
									|| !MediaType.TEXT_HTML_VALUE.equals(((EmailContent) msgBody).getContentType());
						})
						.channel(EMAIL_SENDING_FLOW_INPUT_CHANNEL_ID))
			);
	}
	
	@Bean
	public EmailMessageAggregationStrategy multiLocalesAggregationStrategy() {
		return new EmailMessageAggregationStrategy();
	}
	

}
