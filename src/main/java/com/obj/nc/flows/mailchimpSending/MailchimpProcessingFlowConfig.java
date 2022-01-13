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

package com.obj.nc.flows.mailchimpSending;

import com.obj.nc.channels.ChannelFactory;
import com.obj.nc.functions.processors.messagePersister.MessageAndEndpointPersister;
import com.obj.nc.functions.processors.messagePersister.MessagePersister;
import com.obj.nc.functions.processors.messageTemplating.config.TrackingConfigProperties;
import com.obj.nc.functions.processors.messageTracking.MailchimpReadTrackingDecorator;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpMessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID;

@Configuration
@RequiredArgsConstructor
public class MailchimpProcessingFlowConfig {
    
    public final static String MAILCHIMP_PROCESSING_FLOW_ID = "MAILCHIMP_PROCESSING_FLOW_ID";
    public final static String MAILCHIMP_PROCESSING_FLOW_INPUT_CHANNEL_ID = MAILCHIMP_PROCESSING_FLOW_ID + "_INPUT";
    public static final String MAILCHIMP_PROCESSING_FLOW_OUTPUT_CHANNEL_ID = MAILCHIMP_PROCESSING_FLOW_ID + "_OUTPUT";
    
    private final MailchimpMessageSender mailchimpMessageSender;
    private final MailchimpReadTrackingDecorator readTrackingDecorator;
    private final TrackingConfigProperties trackingConfigProperties;
    private final MessagePersister messagePersister;
    private final MessageAndEndpointPersister messageAndEndpointPersister;
    private final ChannelFactory channelFactory;
    
    @Bean(MAILCHIMP_PROCESSING_FLOW_INPUT_CHANNEL_ID)
    public MessageChannel mailchimpProcessingInputChangel() {
        return channelFactory.getPublishSubscribeChannel(MAILCHIMP_PROCESSING_FLOW_INPUT_CHANNEL_ID);
    }
    
    @Bean(MAILCHIMP_PROCESSING_FLOW_ID)
    public IntegrationFlow mailchimpProcessingFlowDefinition() {
        return IntegrationFlows
                .from(MAILCHIMP_PROCESSING_FLOW_INPUT_CHANNEL_ID)
                .handle(messageAndEndpointPersister)
                .routeToRecipients(spec -> spec
                        .recipientFlow(source -> trackingConfigProperties.isEnabled(),
                                trackingSubflow -> trackingSubflow
                                        .handle(readTrackingDecorator)
                                        .handle(messagePersister)
                                        .channel(internalEmailSendFlowDefinition().getInputChannel()))
                        .defaultOutputChannel(internalEmailSendFlowDefinition().getInputChannel()))
                .get();
    }
    
    @Bean("INTERNAL_MAILCHIMP_SEND_FLOW_ID")
    public IntegrationFlow internalEmailSendFlowDefinition() {
        return flow -> flow
                .handle(mailchimpMessageSender)
                .wireTap(flowConfig -> flowConfig.channel(DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID))
                .channel(mailchimpSendOutputChannel()); 
    }
    
    @Bean(MAILCHIMP_PROCESSING_FLOW_OUTPUT_CHANNEL_ID)
    public MessageChannel mailchimpSendOutputChannel() {
        return channelFactory.getPublishSubscribeChannel(MAILCHIMP_PROCESSING_FLOW_OUTPUT_CHANNEL_ID);
    }
    
}
