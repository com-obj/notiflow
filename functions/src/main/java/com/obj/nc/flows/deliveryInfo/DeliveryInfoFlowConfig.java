package com.obj.nc.flows.deliveryInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.functions.processors.deliveryInfo.DeliveryInfoFailedGenerator;
import com.obj.nc.functions.processors.deliveryInfo.DeliveryInfoProcessingGenerator;
import com.obj.nc.functions.processors.deliveryInfo.DeliveryInfoSendGenerator;
import com.obj.nc.functions.processors.errorHandling.FailedPaylodExtractor;
import com.obj.nc.functions.sink.deliveryInfoPersister.DeliveryInfoPersister;

import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class DeliveryInfoFlowConfig {
	
	public final static String DELIVERY_INFO_SEND_FLOW_ID = "DELIVERY_INFO_SEND_FLOW_ID";
	public final static String DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID = DELIVERY_INFO_SEND_FLOW_ID + "_INPUT";
	
	public final static String DELIVERY_INFO_PROCESSING_FLOW_ID = "DELIVERY_INFO_PROCESSING_FLOW_ID";
	public final static String DELIVERY_INFO_PROCESSING_FLOW_INPUT_CHANNEL_ID = DELIVERY_INFO_PROCESSING_FLOW_ID + "_INPUT";
	
	public final static String DELIVERY_INFO_FAILED_FLOW_ID = "DELIVERY_INFO_FAILED_FLOW_ID";
	public final static String DELIVERY_INFO_FAILED_FLOW_INPUT_CHANNEL_ID = DELIVERY_INFO_FAILED_FLOW_ID + "_INPUT";

	@Autowired private DeliveryInfoPersister deliveryPersister;
	@Autowired private DeliveryInfoSendGenerator deliveryInfoSendGenerator;
	@Autowired private DeliveryInfoFailedGenerator deliveryInfoFailedGenerator;
	@Autowired private DeliveryInfoProcessingGenerator deliveryInfoProcessingGenerator;
	@Autowired private FailedPaylodExtractor extractor;

	//Default channel for errorMessages used by spring
	@Autowired
	@Qualifier("errorChannel")
	private PublishSubscribeChannel errorChannel;

    @Bean
    public IntegrationFlow deliveryInfoFailedFlow(ThreadPoolTaskScheduler executor) {
        return 
        	IntegrationFlows.from(deliveryInfoFailedInputChannel(executor))
        		.handle( extractor )
        		.filter(p-> p instanceof BasePayload)
				.handle(deliveryInfoFailedGenerator)
				.split()
				.handle(deliveryPersister)
        		.get();
    }
    
    @Bean
    public IntegrationFlow deliveryInfoSendFlow(ThreadPoolTaskScheduler executor) {
        return 
        	IntegrationFlows.from(deliveryInfoSendInputChannel(executor))
				.handle(deliveryInfoSendGenerator)
				.split()
				.handle(deliveryPersister)
        		.get();
    }
    
    @Bean
    public IntegrationFlow deliveryInfoProcessingFlow(ThreadPoolTaskScheduler executor) {
        return 
        	IntegrationFlows.from(deliveryInfoProcessingInputChannel(executor))
				.handle(deliveryInfoProcessingGenerator)
				.split()
				.handle(deliveryPersister)
        		.get();
    }
    
	@Bean(DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel deliveryInfoSendInputChannel(ThreadPoolTaskScheduler executor) {
		return new PublishSubscribeChannel(executor);
	}
	
	@Bean(DELIVERY_INFO_PROCESSING_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel deliveryInfoProcessingInputChannel(ThreadPoolTaskScheduler executor) {
		return new PublishSubscribeChannel(executor);
	}
	
	@Bean(DELIVERY_INFO_FAILED_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel deliveryInfoFailedInputChannel(ThreadPoolTaskScheduler executor) {
		return new PublishSubscribeChannel(executor);
	}

}
