/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.flows.emailFormattingAndSending;

import com.obj.nc.domain.content.TemplateWithModelContent;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.messageAggregator.aggregations.EmailMessageAggregationStrategy;
import com.obj.nc.functions.processors.messagePersister.MessageAndEndpointPersister;
import com.obj.nc.functions.processors.messagePersister.MessagePersister;
import com.obj.nc.functions.processors.messageTemplating.EmailTemplateFormatter;
import com.obj.nc.functions.processors.messageTemplating.config.TrackingConfigProperties;
import com.obj.nc.functions.processors.messageTracking.EmailReadTrackingDecorator;
import com.obj.nc.functions.processors.senders.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.PollerSpec;
import org.springframework.integration.dsl.Pollers;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowProperties.MULTI_LOCALES_MERGE_STRATEGY.MERGE;

@Configuration
@RequiredArgsConstructor
public class EmailProcessingFlowConfig {
	
	private final EmailSender emailSender;
	private final EmailTemplateFormatter emailFormatter;
	private final EmailReadTrackingDecorator readTrackingDecorator;
	private final TrackingConfigProperties trackingConfigProperties;
	private final EmailProcessingFlowProperties properties;
	private final MessagePersister messagePersister;
	private final MessageAndEndpointPersister messageAndEndpointPersister;
	private final ThreadPoolTaskScheduler executor;
	private final EmailMessageAggregationStrategy emailMessageAggregationStrategy;
	
	public final static String EMAIL_FORMAT_AND_SEND_FLOW_ID = "EMAIL_FORMAT_AND_SEND_FLOW_ID";
	public final static String EMAIL_FORMAT_AND_SEND_FLOW_INPUT_CHANNEL_ID = EMAIL_FORMAT_AND_SEND_FLOW_ID + "_INPUT";
	
	public final static String EMAIL_SEND_FLOW_ID = "EMAIL_SEND_FLOW_ID";
	public final static String EMAIL_SEND_FLOW_INPUT_CHANNEL_ID = EMAIL_SEND_FLOW_ID + "_INPUT";

	public final static String EMAIL_SEND_FLOW_OUTPUT_CHANNEL_ID = EMAIL_SEND_FLOW_ID + "_OUTPUT";

	public final static String INTERNAL_EMAIL_SEND_FLOW_ID = "INTERNAL_EMAIL_SEND_FLOW_ID";
	public final static String INTERNAL_EMAIL_SEND_FLOW_INPUT_CHANNEL_ID = INTERNAL_EMAIL_SEND_FLOW_ID + "_INPUT";

	public final static String EMAIL_SENDER_MESSAGE_HANDLER = "EMAIL_SENDER_MESSAGE_HANDLER";
	public final static String EMAIL_SENDER_MESSAGE_HANDLER_POLLER = "EMAIL_SENDER_MESSAGE_HANDLER_POLLER";

	@Bean(EMAIL_FORMAT_AND_SEND_FLOW_ID)
	public IntegrationFlow emailFormatAndSendFlowDefinition() {
		return IntegrationFlows
				.from(emailFormatAndSendInputChannel())
				.handle(messageAndEndpointPersister)
				.routeToRecipients(spec -> spec
						.<Message<?>>recipient(
								internalEmailFormatFlowDefinition().getInputChannel(),
								m -> m.getBody() instanceof TemplateWithModelContent)
						.defaultOutputChannel(emailSendFlowDefinition().getInputChannel()))
				.get();
	}
	
	@Bean("INTERNAL_EMAIL_FORMAT_FLOW_ID")
	public IntegrationFlow internalEmailFormatFlowDefinition() {
		return flow -> flow
				.publishSubscribeChannel(subscription  -> subscription
						//format email and merge if multilanguage
						.subscribe(aggregateMultilangFlow -> aggregateMultilangFlow
								.filter(m-> MERGE.equals(properties.getMultiLocalesMergeStrategy()))
								.handle(emailFormatter)
								.split()
								.handle(messagePersister)
								.aggregate()
								.handle(emailMessageAggregationStrategy)
								.handle(messagePersister)
								.channel(emailSendFlowDefinition().getInputChannel()))
						//format and split if multilanguage
						.subscribe(mesagePerLocaleFlow -> mesagePerLocaleFlow
								.filter(m-> !MERGE.equals(properties.getMultiLocalesMergeStrategy()))
								.split(emailFormatter)
								.handle(messagePersister)
								.channel(emailSendFlowDefinition().getInputChannel()))
				);
	}
	
	@Bean(EMAIL_SEND_FLOW_ID)
	public IntegrationFlow emailSendFlowDefinition() {
		return IntegrationFlows
				.from(emailSendInputChannel())
				.handle(messageAndEndpointPersister)
				.routeToRecipients(spec -> spec
						.recipientFlow((Message<EmailContent> source) -> trackingConfigProperties.isEnabled() 
										&& MediaType.TEXT_HTML_VALUE.equals(source.getBody().getContentType()),
								trackingSubflow -> trackingSubflow
										.handle(readTrackingDecorator)
										.handle(messagePersister)
										.channel(internalEmailSendInputChannel()))
						.defaultOutputChannel(internalEmailSendInputChannel()))
				.get();
	}

	/*
		Microsoft SMTP server has throttling to only accept at most 3 concurrent mail sending connections
		therefore we need to create delays between message-sends. We use queue channel and poller to do so.
		Default polling rate is 0ms, because this is a special case - not every SMTP server has this throttling
	 */
	@Bean(INTERNAL_EMAIL_SEND_FLOW_ID)
	public IntegrationFlow internalEmailSendFlowDefinition() {
		return IntegrationFlows
				.from(internalEmailSendInputChannel())
				.handle(EmailMessage.class, (p, h) -> emailSender.apply(p),
						c -> c.id(EMAIL_SENDER_MESSAGE_HANDLER).poller(emailSenderPoller()))
				.wireTap(flowConfig -> flowConfig.channel(DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID))
				.channel(emailSendOutputChannel())
				.get();
	}

	@Bean(EMAIL_FORMAT_AND_SEND_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel emailFormatAndSendInputChannel() {
		return new PublishSubscribeChannel(executor);
	}
	
	@Bean(EMAIL_SEND_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel emailSendInputChannel() {
		return new PublishSubscribeChannel(executor);
	}

	@Bean(INTERNAL_EMAIL_SEND_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel internalEmailSendInputChannel() {
		return new QueueChannel();
	}
	
	@Bean(EMAIL_SEND_FLOW_OUTPUT_CHANNEL_ID)
	public MessageChannel emailSendOutputChannel() {
		return new PublishSubscribeChannel(executor);
	}

	@Bean(EMAIL_SENDER_MESSAGE_HANDLER_POLLER)
	public PollerSpec emailSenderPoller() {
		return Pollers.fixedDelay(properties.getDelaySendingPollRateMillis())
				.maxMessagesPerPoll(properties.getDelaySendingMaxMessagesPerPoll())
				.taskExecutor(executor);
	}
	
}
