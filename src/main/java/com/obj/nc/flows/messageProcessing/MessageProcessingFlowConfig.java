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

package com.obj.nc.flows.messageProcessing;

import com.obj.nc.domain.message.*;
import com.obj.nc.functions.processors.endpointPersister.EndpointPersister;
import com.obj.nc.functions.processors.messageBuilder.MessageByRecipientTokenizer;
import com.obj.nc.functions.processors.messagePersister.MessagePersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.DELIVERY_INFO_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowConfig.EMAIL_FORMAT_AND_SEND_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowConfig.EMAIL_SEND_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.mailchimpSending.MailchimpProcessingFlowConfig.MAILCHIMP_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.mailchimpSending.TemplatedMailchimpMessageProcessingFlowConfig.MAILCHIMP_TEMPLATE_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.pushProcessing.PushProcessingFlowConfig.PUSH_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.slackMessageProcessingFlow.SlackMessageProcessingFlowConfig.SLACK_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.smsFormattingAndSending.SmsProcessingFlowConfig.SMS_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.teamsMessageProcessing.TeamsMessageProcessingFlowConfig.TEAMS_PROCESSING_FLOW_INPUT_CHANNEL_ID;

@Configuration
public class MessageProcessingFlowConfig {

    @Autowired
    MessageByRecipientTokenizer<?> messageByRecipientTokenizer;
    @Autowired
    MessagePersister messagePersister;
    @Autowired
    EndpointPersister endpointPersister;


    public final static String MESSAGE_PROCESSING_FLOW_ID = "MESSAGE_PROCESSING_FLOW_ID";
    public final static String MESSAGE_PROCESSING_FLOW_INPUT_CHANNEL_ID = MESSAGE_PROCESSING_FLOW_ID + "_INPUT";

    @Bean(MESSAGE_PROCESSING_FLOW_INPUT_CHANNEL_ID)
    public PublishSubscribeChannel messageProcessingInputChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean(MESSAGE_PROCESSING_FLOW_ID)
    public IntegrationFlow messageProcessingFlowDefinition() {
        return IntegrationFlows
                .from(messageProcessingInputChannel())
                .handle(endpointPersister)
                .handle(messagePersister)
                .transform(messageByRecipientTokenizer)
                .split()
                .handle(endpointPersister)
                .handle(messagePersister) //need to persist, otherwise delivery info will have invalid reference
                .wireTap(flowConfig ->
                        flowConfig.channel(DELIVERY_INFO_PROCESSING_FLOW_INPUT_CHANNEL_ID)
                )
                .routeToRecipients(spec -> spec.
                        recipient(EMAIL_SEND_FLOW_INPUT_CHANNEL_ID, m -> m instanceof EmailMessage).
                        recipient(EMAIL_FORMAT_AND_SEND_FLOW_INPUT_CHANNEL_ID, m -> m instanceof EmailMessageTemplated).
                        recipient(SMS_PROCESSING_FLOW_INPUT_CHANNEL_ID, m -> m instanceof SmsMessageTemplated).
                        recipient(MAILCHIMP_TEMPLATE_PROCESSING_FLOW_INPUT_CHANNEL_ID, m -> m instanceof TemplatedMailchimpMessage).
                        recipient(MAILCHIMP_PROCESSING_FLOW_INPUT_CHANNEL_ID, m -> m instanceof MailchimpMessage).
                        recipient(PUSH_PROCESSING_FLOW_INPUT_CHANNEL_ID, m -> m instanceof PushMessage).
                        recipient(SLACK_PROCESSING_FLOW_INPUT_CHANNEL_ID, m -> m instanceof SlackMessage).
                        recipient(TEAMS_PROCESSING_FLOW_INPUT_CHANNEL_ID, m -> m instanceof TeamsMessage).
                        defaultOutputToParentFlow()
                )
                .get();
    }

}
