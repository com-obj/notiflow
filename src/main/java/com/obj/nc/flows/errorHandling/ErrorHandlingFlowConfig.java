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

package com.obj.nc.flows.errorHandling;

import com.obj.nc.flows.deliveryInfo.DeliveryInfoFlow;
import com.obj.nc.functions.processors.errorHandling.SpringMessageToFailedPayloadFunction;
import com.obj.nc.functions.processors.failedPaylodPersister.FailedPayloadPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler.Level;
import org.springframework.messaging.MessageHeaders;

@Configuration
public class ErrorHandlingFlowConfig {
	
	public static final String ERROR_CHANNEL_NAME = "errorChannel";
	public static final String ERROR_CHANNEL_FOR_ERROR_HANDLING_FLOW_NAME = "errorChannelTerminal";
	
	//Default channel for errorMessages used by spring
	@Qualifier(ERROR_CHANNEL_NAME)
	@Autowired private PublishSubscribeChannel errorChannel;

	@Qualifier(ERROR_CHANNEL_FOR_ERROR_HANDLING_FLOW_NAME)
	@Autowired private PublishSubscribeChannel errorChannelTerminal;

    @Autowired private SpringMessageToFailedPayloadFunction failedPayloadTransformer;
	@Autowired private FailedPayloadPersister failedPayloadPersister;
	@Autowired private DeliveryInfoFlow deliveryInfoFlow;
	
    /**
     * Error handling must be processed in single thread (no publish/subscribe channels with executors or similar are used). 
     * In case exception is throw in the error handling it self this prevents potential error handling infinite loop cycles. Check MessagePublishingErrorHandler.handleError		
     */
    @Bean
    public IntegrationFlow errorPayloadReceivedFlowConfig() {
        return 
        	IntegrationFlows
                .from(errorChannel)
                .log(Level.WARN, "ERROR HANDLING")
                .enrichHeaders(h -> h.header(MessageHeaders.ERROR_CHANNEL, ERROR_CHANNEL_FOR_ERROR_HANDLING_FLOW_NAME, true))
				.handle(failedPayloadTransformer)                
                .handle(failedPayloadPersister)
                .handle(deliveryInfoFlow, "createAndPersistFailedDeliveryInfo")
                .log(Level.DEBUG, "ERROR HANDLING FINISHED")
        		.get();
    }

    @Bean
    public IntegrationFlow errorPayloadReceivedFromErrorHandlingFlowConfig() {
        return 
        	IntegrationFlows
                .from(errorChannelTerminal)
                .log(Level.ERROR, "ERROR HANDLING ocurred in error handling flow")
        		.get();
    }

    @Bean(ERROR_CHANNEL_FOR_ERROR_HANDLING_FLOW_NAME)
    PublishSubscribeChannel errorChannelTerminal() {
        return new PublishSubscribeChannel();
    }

}
