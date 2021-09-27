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

package com.obj.nc.flows.pushProcessing;

import com.obj.nc.functions.processors.endpointPersister.EndpointPersister;
import com.obj.nc.functions.processors.messagePersister.MessagePersister;
import com.obj.nc.functions.processors.senders.PushSender;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID;

@Configuration
@RequiredArgsConstructor
public class PushProcessingFlowConfig {
	
	public final static String PUSH_SEND_FLOW_ID = "PUSH_SEND_FLOW_ID";
	public final static String PUSH_SEND_FLOW_INPUT_CHANNEL_ID = PUSH_SEND_FLOW_ID + "_INPUT";
	public final static String PUSH_SEND_FLOW_OUTPUT_CHANNEL_ID = PUSH_SEND_FLOW_ID + "_OUTPUT";
	
	private final PushSender pushSender;
	private final MessagePersister messagePersister;
	private final EndpointPersister endpointPersister; 
	private final ThreadPoolTaskScheduler executor;
	
	@Bean(PUSH_SEND_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel pushSendInputChannel() {
		return new PublishSubscribeChannel(executor);
	}
	
	@Bean(PUSH_SEND_FLOW_OUTPUT_CHANNEL_ID)
	public MessageChannel pushSendOutputChannel() {
		return new PublishSubscribeChannel(executor);
	}
	
	@Bean(PUSH_SEND_FLOW_ID)
	public IntegrationFlow pushSendFlow() {
		return IntegrationFlows
				.from(pushSendInputChannel())
				.handle(endpointPersister)
				.handle(messagePersister)
				.handle(pushSender)
				.wireTap(flowConfig -> flowConfig.channel(DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID))
				.channel(pushSendOutputChannel())
				.get();
	}
	
}
