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

import com.obj.nc.functions.processors.delivery.MessageAndEndpointPersister;
import com.obj.nc.functions.processors.senders.mailchimp.TemplatedMailchimpMessageSender;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@RequiredArgsConstructor
public class TemplatedMailchimpMessageProcessingFlowConfig {
    
    public final static String MAILCHIMP_TEMPLATE_PROCESSING_FLOW_ID = "MAILCHIMP_TEMPLATE_PROCESSING_FLOW_ID";
    public final static String MAILCHIMP_TEMPLATE_PROCESSING_FLOW_INPUT_CHANNEL_ID = MAILCHIMP_TEMPLATE_PROCESSING_FLOW_ID + "_INPUT";
    
    private final TemplatedMailchimpMessageSender templatedMailchimpMessageSender;
    private final PaylaodLoggerSinkConsumer logConsumer;
    private final MessageAndEndpointPersister persister;
    private final ThreadPoolTaskScheduler executor;
    
    @Bean(MAILCHIMP_TEMPLATE_PROCESSING_FLOW_INPUT_CHANNEL_ID)
    public MessageChannel mailchimpTemplateProcessingInputChangel() {
        return new PublishSubscribeChannel(executor);
    }
    
    @Bean(MAILCHIMP_TEMPLATE_PROCESSING_FLOW_ID)
    public IntegrationFlow mailchimpTemplateProcessingFlowDefinition() {
        return IntegrationFlows
                .from(MAILCHIMP_TEMPLATE_PROCESSING_FLOW_INPUT_CHANNEL_ID)
                .handle(persister)
                .handle(templatedMailchimpMessageSender)
                .handle(logConsumer)
                .get();
    }
    
}
