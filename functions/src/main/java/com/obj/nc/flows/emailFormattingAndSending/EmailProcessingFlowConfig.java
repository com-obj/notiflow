package com.obj.nc.flows.emailFormattingAndSending;

import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowProperties.MULTI_LOCALES_MERGE_STRATEGY.MERGE;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class EmailProcessingFlowConfig {
	
	private final EmailSender emailSender;
	private final PaylaodLoggerSinkConsumer logConsumer;
	private final EmailTemplateFormatter emailFormatter;
	private final EmailProcessingFlowProperties properties;
	private final MessagePersister messagePersister;
	private final ThreadPoolTaskScheduler executor;
	
	public final static String EMAIL_FORMAT_AND_SEND_FLOW_ID = "EMAIL_FORMAT_AND_SEND_FLOW_ID";
	public final static String EMAIL_FROMAT_AND_SEND_INPUT_CHANNEL_ID = EMAIL_FORMAT_AND_SEND_FLOW_ID + "_INPUT";

	public final static String EMAIL_SENDING_FLOW_ID = "EMAIL_SENDING_FLOW_ID";
	public final static String EMAIL_SENDING_FLOW_INPUT_CHANNEL_ID = EMAIL_SENDING_FLOW_ID + "_INPUT";
	
	public final static String EMAIL_FORMATING_FLOW_ID = "EMAIL_FORMATING_FLOW_ID";
	public final static String EMAIL_FORMATING_INPUT_CHANNEL_ID = EMAIL_FORMATING_FLOW_ID + "_INPUT";

	@Bean(EMAIL_FROMAT_AND_SEND_INPUT_CHANNEL_ID)
	public MessageChannel emailProcessingInputChangel() {
		return new PublishSubscribeChannel(executor);
	}
	
	@Bean(EMAIL_SENDING_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel emailSedningInputChangel() {
		return new PublishSubscribeChannel(executor);
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
								m -> ((Message) m).getBody().getMessage() instanceof TemplateWithModelContent)
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
				.handle(logConsumer)
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
							.channel(EMAIL_SENDING_FLOW_INPUT_CHANNEL_ID))
					//format and split if multilanguage
					.subscribe(mesagePerLocaleFlow -> mesagePerLocaleFlow
							.filter(m-> !MERGE.equals(properties.getMultiLocalesMergeStrategy()))
							.split(emailFormatter)
							.channel(EMAIL_SENDING_FLOW_INPUT_CHANNEL_ID))	
				);

	}
	
	@Bean
	public EmailMessageAggregationStrategy multiLocalesAggregationStrategy() {
		return new EmailMessageAggregationStrategy();
	}
	

}
