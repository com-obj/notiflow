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

package com.obj.nc.flows.slackMessageProcessingFlow;

import com.obj.nc.channels.ChannelFactory;
import com.obj.nc.functions.processors.messagePersister.MessageAndEndpointPersister;
import com.obj.nc.functions.processors.senders.slack.SlackMessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID;

@Configuration
@RequiredArgsConstructor
public class SlackMessageProcessingFlowConfig {
    public final static String SLACK_PROCESSING_FLOW_ID = "SLACK_PROCESSING_FLOW_ID";
    public final static String SLACK_PROCESSING_FLOW_INPUT_CHANNEL_ID = SLACK_PROCESSING_FLOW_ID + "_INPUT";
    public final static String SLACK_PROCESSING_FLOW_OUTPUT_CHANNEL_ID = SLACK_PROCESSING_FLOW_ID + "_OUTPUT";

    private final SlackMessageSender slackMessageSender;
    private final MessageAndEndpointPersister persister;
    private final ChannelFactory channelFactory;

    @Bean(SLACK_PROCESSING_FLOW_INPUT_CHANNEL_ID)
    public MessageChannel sendInputChannel() {
        return channelFactory.getPublishSubscribeChannel(SLACK_PROCESSING_FLOW_INPUT_CHANNEL_ID);
    }

    @Bean(SLACK_PROCESSING_FLOW_OUTPUT_CHANNEL_ID)
    public MessageChannel sendOutputChannel() {
        return channelFactory.getPublishSubscribeChannel(SLACK_PROCESSING_FLOW_OUTPUT_CHANNEL_ID);
    }

    @Bean(SLACK_PROCESSING_FLOW_ID)
    public IntegrationFlow pushSendFlow() {
        return IntegrationFlows
                .from(sendInputChannel())
                .handle(persister)
                .handle(slackMessageSender)
                .wireTap(flowConfig -> flowConfig.channel(DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID))
                .channel(sendOutputChannel())
                .get();
    }
}
