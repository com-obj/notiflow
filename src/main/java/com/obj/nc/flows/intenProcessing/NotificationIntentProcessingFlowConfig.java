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

package com.obj.nc.flows.intenProcessing;

import com.obj.nc.functions.processors.endpointPersister.EndpointPersister;
import com.obj.nc.functions.processors.intentPersister.NotificationIntentPersister;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromIntentGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

import static com.obj.nc.flows.messageProcessing.MessageProcessingFlowConfig.MESSAGE_PROCESSING_FLOW_INPUT_CHANNEL_ID;

@Configuration
public class NotificationIntentProcessingFlowConfig {
		
	@Autowired private MessagesFromIntentGenerator generateMessagesFromIntent;
	@Autowired private NotificationIntentPersister notificationIntentPersister;
	// @Autowired private EndpointPersister endpointPersister; 
	
	public final static String INTENT_PROCESSING_FLOW_ID = "INTENT_PROCESSING_FLOW_ID";
	public final static String INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID = INTENT_PROCESSING_FLOW_ID + "_INPUT";
	
	@Bean(INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel intentProcessingInputChannel() {
		return new PublishSubscribeChannel();
	}
	
	@Bean(INTENT_PROCESSING_FLOW_ID)
	public IntegrationFlow intentProcessingFlowDefinition() {
		return IntegrationFlows
				.from(intentProcessingInputChannel())
				// .handle(endpointPersister)
				.handle(notificationIntentPersister)	
                //TODO: users could subscribe to intents. NC would be able to calculate recipients based on these settings. Currently recipients has to be provided
				.transform(generateMessagesFromIntent)
				.split()
				.channel(MESSAGE_PROCESSING_FLOW_INPUT_CHANNEL_ID)
				.get();
	}
	
}
