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

package com.obj.nc.flows.deliveryInfo;

import com.obj.nc.channels.ChannelFactory;
import com.obj.nc.functions.processors.deliveryInfo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

@Configuration
public class DeliveryInfoFlowConfig {
	
	public final static String DELIVERY_INFO_SEND_FLOW_ID = "DELIVERY_INFO_SEND_FLOW_ID";
	public final static String DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID = DELIVERY_INFO_SEND_FLOW_ID + "_INPUT";
    public final static String DELIVERY_INFO_SEND_FLOW_OUTPUT_CHANNEL_ID = DELIVERY_INFO_SEND_FLOW_ID + "_OUTPUT";
	
	public final static String DELIVERY_INFO_PROCESSING_FLOW_ID = "DELIVERY_INFO_PROCESSING_FLOW_ID";
	public final static String DELIVERY_INFO_PROCESSING_FLOW_INPUT_CHANNEL_ID = DELIVERY_INFO_PROCESSING_FLOW_ID + "_INPUT";
	public final static String DELIVERY_INFO_PROCESSING_FLOW_OUTPUT_CHANNEL_ID = DELIVERY_INFO_PROCESSING_FLOW_ID + "_OUTPUT";
	
	public final static String DELIVERY_INFO_FAILED_FLOW_ID = "DELIVERY_INFO_FAILED_FLOW_ID";
	public final static String DELIVERY_INFO_FAILED_FLOW_INPUT_CHANNEL_ID = DELIVERY_INFO_FAILED_FLOW_ID + "_INPUT";
	public final static String DELIVERY_INFO_FAILED_FLOW_OUTPUT_CHANNEL_ID = DELIVERY_INFO_FAILED_FLOW_ID + "_OUTPUT";
	
	public final static String DELIVERY_INFO_READ_FLOW_ID = "DELIVERY_INFO_READ_FLOW_ID";
	public final static String DELIVERY_INFO_READ_FLOW_INPUT_CHANNEL_ID = DELIVERY_INFO_READ_FLOW_ID + "_INPUT";
    public final static String DELIVERY_INFO_READ_FLOW_OUTPUT_CHANNEL_ID = DELIVERY_INFO_READ_FLOW_ID + "_OUTPUT";

	@Autowired private DeliveryInfoSendTransformer deliveryTransformer;
	@Autowired private DeliveryInfoPersister deliveryInfoPersister;
	@Autowired private DeliveryInfoSendGenerator deliveryInfoSendGenerator;
	@Autowired private DeliveryInfoReadGenerator deliveryInfoReadGenerator;
	@Autowired private DeliveryInfoFailedGenerator deliveryInfoFailedGenerator;
	@Autowired private DeliveryInfoProcessingGenerator deliveryInfoProcessingGenerator;
	@Autowired private ChannelFactory channelFactory;

    @Bean
    public IntegrationFlow deliveryInfoFailedFlow() {
        return 
        	IntegrationFlows.from(deliveryInfoFailedInputChannel())
				.handle(deliveryInfoFailedGenerator)
				.handle(deliveryInfoPersister)
				.channel(DELIVERY_INFO_FAILED_FLOW_OUTPUT_CHANNEL_ID)
        		.get();
    }

    @Bean(DELIVERY_INFO_FAILED_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel deliveryInfoFailedInputChannel() {
		return channelFactory.getPublishSubscribeChannel(DELIVERY_INFO_FAILED_FLOW_INPUT_CHANNEL_ID);
	}

    @Bean(DELIVERY_INFO_FAILED_FLOW_OUTPUT_CHANNEL_ID)
	public MessageChannel deliveryInfoFailedOutputChannel() {
		return channelFactory.getPublishSubscribeChannel(DELIVERY_INFO_FAILED_FLOW_OUTPUT_CHANNEL_ID);
	}
    ///////////////////////////////////////////////////////////////////////////
    
    @Bean
    public IntegrationFlow deliveryInfoSendFlow() {
        return 
        	IntegrationFlows.from(deliveryInfoSendInputChannel())
				.handle(deliveryInfoSendGenerator)
				.split()
				.handle(deliveryTransformer)
				.handle(deliveryInfoPersister)
				.channel(DELIVERY_INFO_SEND_FLOW_OUTPUT_CHANNEL_ID)
        		.get();
    }

    @Bean(DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel deliveryInfoSendInputChannel() {
		return channelFactory.getPublishSubscribeChannel(DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID);
	}

    @Bean(DELIVERY_INFO_SEND_FLOW_OUTPUT_CHANNEL_ID)
	public MessageChannel deliveryInfoSendOutputChannel() {
		return channelFactory.getPublishSubscribeChannel(DELIVERY_INFO_SEND_FLOW_OUTPUT_CHANNEL_ID);
	}
    ///////////////////////////////////////////////////////////////////////////
    
    @Bean
    public IntegrationFlow deliveryInfoProcessingFlow() {
        return 
        	IntegrationFlows.from(deliveryInfoProcessingInputChannel())
				.handle(deliveryInfoProcessingGenerator)
				.split()
				.handle(deliveryTransformer)
				.handle(deliveryInfoPersister)
				.channel(DELIVERY_INFO_PROCESSING_FLOW_OUTPUT_CHANNEL_ID)
        		.get();
    }

    @Bean(DELIVERY_INFO_PROCESSING_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel deliveryInfoProcessingInputChannel() {
		return channelFactory.getPublishSubscribeChannel(DELIVERY_INFO_PROCESSING_FLOW_INPUT_CHANNEL_ID);
	}

    @Bean(DELIVERY_INFO_PROCESSING_FLOW_OUTPUT_CHANNEL_ID)
	public MessageChannel deliveryInfoProcessingOutputChannel() {
		return channelFactory.getPublishSubscribeChannel(DELIVERY_INFO_PROCESSING_FLOW_OUTPUT_CHANNEL_ID);
	}	
    ///////////////////////////////////////////////////////////////////////////

	@Bean
	public IntegrationFlow deliveryInfoReadFlow() {
		return
			IntegrationFlows.from(deliveryInfoReadInputChannel())
				.handle(deliveryInfoReadGenerator)
				.split()
				.handle(deliveryTransformer)
				.handle(deliveryInfoPersister)
				.channel(DELIVERY_INFO_READ_FLOW_OUTPUT_CHANNEL_ID)
				.get();
	}

    @Bean(DELIVERY_INFO_READ_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel deliveryInfoReadInputChannel() {
		return channelFactory.getPublishSubscribeChannel(DELIVERY_INFO_READ_FLOW_INPUT_CHANNEL_ID);
	}

    @Bean(DELIVERY_INFO_READ_FLOW_OUTPUT_CHANNEL_ID)
	public MessageChannel deliveryInfoReadOutputChannel() {
		return channelFactory.getPublishSubscribeChannel(DELIVERY_INFO_READ_FLOW_OUTPUT_CHANNEL_ID);
	}
	
}
