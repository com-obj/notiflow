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

package com.obj.nc.flows.dataSources;

import com.obj.nc.functions.processors.genericDataConverter.ExtensionsBasedGenericData2EventConverter;
import com.obj.nc.functions.processors.genericDataConverter.ExtensionsBasedGenericData2NotificationConverter;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersister;
import com.obj.nc.routers.MessageOrIntentRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@RequiredArgsConstructor
public class GenericDataConvertingFlowConfiguration {
    
    public static final String GENERIC_DATA_CONVERTING_FLOW_ID = "GENERIC_DATA_CONVERTING_FLOW_ID";
    public static final String GENERIC_DATA_CONVERTING_FLOW_ID_INPUT_CHANNEL_ID = GENERIC_DATA_CONVERTING_FLOW_ID + "_INPUT";
    
    private final ExtensionsBasedGenericData2EventConverter genericData2EventsConverter;
    private final ExtensionsBasedGenericData2NotificationConverter genericData2NotificationsConverter;
    private final MessageOrIntentRouter messageOrIntentRouter;
    private final GenericEventPersister genericEventPersister;
    private final ThreadPoolTaskScheduler executor;
    
    @Bean(GENERIC_DATA_CONVERTING_FLOW_ID_INPUT_CHANNEL_ID)
    public PublishSubscribeChannel genericDataConvertingFlowInputChannel() {
        return new PublishSubscribeChannel(executor);
    }
    
    @Bean(GENERIC_DATA_CONVERTING_FLOW_ID)
    public IntegrationFlow genericDataConvertingFlow() {
        return IntegrationFlows
                .from(genericDataConvertingFlowInputChannel())
                .publishSubscribeChannel(spec -> spec
                        .subscribe(subFlow -> subFlow
                                .handle(genericData2EventsConverter)
                                .split()
                                .handle(genericEventPersister))
                        .subscribe(subFlow -> subFlow
                                .handle(genericData2NotificationsConverter)
                                .split()
                                .route(messageOrIntentRouter))
                )
                .get();
    }
    
}
