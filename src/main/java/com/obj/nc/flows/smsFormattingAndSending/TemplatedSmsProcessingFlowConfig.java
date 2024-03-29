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

package com.obj.nc.flows.smsFormattingAndSending;

import com.obj.nc.functions.processors.messagePersister.MessageAndEndpointPersister;
import com.obj.nc.functions.processors.messagePersister.MessagePersister;
import com.obj.nc.functions.processors.messageTemplating.SmsTemplateFormatter;
import com.obj.nc.functions.processors.senders.SmsSender;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID;

@Configuration
@RequiredArgsConstructor
public class TemplatedSmsProcessingFlowConfig {
	
	private final SmsSender smsSender;
	private final SmsTemplateFormatter smsFormatter;
	private final PaylaodLoggerSinkConsumer logConsumer;
	private final MessagePersister messagePersister;
	private final MessageAndEndpointPersister messageAndEndpointPersister;

	public final static String TEMPLATED_SMS_PROCESSING_FLOW_ID = "TEMPLATED_SMS_PROCESSING_FLOW_ID";
	public final static String TEMPLATED_SMS_PROCESSING_FLOW_INPUT_CHANNEL_ID = TEMPLATED_SMS_PROCESSING_FLOW_ID + "_INPUT";
	
	@Bean(TEMPLATED_SMS_PROCESSING_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel smsProcessingInputChangel() {
		return new PublishSubscribeChannel();
	}
	
	@Bean(TEMPLATED_SMS_PROCESSING_FLOW_ID)
	public IntegrationFlow smsProcessingFlowDefinition() {
		return IntegrationFlows
				.from(smsProcessingInputChangel())
				.handle(messageAndEndpointPersister)
				.handle(smsFormatter)
				.split()
				.handle(messagePersister)
				.handle(messageAndEndpointPersister)
				.handle(smsSender)
				.wireTap( flowConfig -> 
					flowConfig.channel(DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID)
				)
				.handle(logConsumer)
				.get();

	}

}
