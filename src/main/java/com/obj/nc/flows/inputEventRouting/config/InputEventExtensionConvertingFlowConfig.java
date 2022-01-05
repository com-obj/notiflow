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

package com.obj.nc.flows.inputEventRouting.config;

import com.obj.nc.domain.IsNotification;
import com.obj.nc.extensions.converters.genericEvent.InputEventConverterExtension;
import com.obj.nc.functions.processors.event2Message.ExtensionsBasedEventConvertor;
import com.obj.nc.routers.MessageOrIntentRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class InputEventExtensionConvertingFlowConfig {
		
	public final static String EVENT_CONVERTING_EXTENSION_FLOW_ID = "EVENT_CONVERTING_EXTENSION_FLOW_ID";
	public final static String EVENT_CONVERTING_EXTENSION_FLOW_ID_INPUT_CHANNEL_ID = EVENT_CONVERTING_EXTENSION_FLOW_ID + "_INPUT";
				
    public static final String GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME = "genericEventSupplierFlowId";
	
	private final MessageOrIntentRouter messageOrIntentRouter;
    
	@Bean(EVENT_CONVERTING_EXTENSION_FLOW_ID_INPUT_CHANNEL_ID)
	public PublishSubscribeChannel messageProcessingInputChannel() {
		return new PublishSubscribeChannel();
	}
	
    @Bean
    public IntegrationFlow extensionBasedRoutingFlow(List<InputEventConverterExtension<? extends IsNotification>> eventProcessors) {
    	return IntegrationFlows
			.from(messageProcessingInputChannel())
			.handle(extensionsBasedEventConvertor(eventProcessors))
			.split()
			.route(messageOrIntentRouter)
			.get();
    }
    
    @Bean
    public ExtensionsBasedEventConvertor extensionsBasedEventConvertor(
    		List<InputEventConverterExtension<? extends IsNotification>> eventProcessors) {
    	return new ExtensionsBasedEventConvertor(eventProcessors);
    }

}
