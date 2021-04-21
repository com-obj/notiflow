package com.obj.nc.flows.errorHandling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

import com.obj.nc.functions.processors.deliveryInfo.DeliveryInfoSendGenerator;
import com.obj.nc.functions.sink.deliveryInfoPersister.DeliveryInfoPersister;

import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class DeliveryInfoFlowConfig {
	
	public final static String DELIVERY_INFO_FLOW_ID = "DELIVERY_INFO_FLOW_ID";
	public final static String DELIVERY_INFO_FLOW_INPUT_CHANNEL_ID = DELIVERY_INFO_FLOW_ID + "_INPUT";

	@Autowired private DeliveryInfoPersister deliveryPersister;
	@Autowired private DeliveryInfoSendGenerator deliveryInfoGenerator;

	
	//Default channel for errorMessages used by spring
	@Autowired
	@Qualifier("errorChannel")
	private PublishSubscribeChannel errorChannel;

//    @Bean
//    public IntegrationFlow customErrorFlow() {
//        return 
//        	IntegrationFlows.from(errorChannel)
//        		.get();
//    }
    
    @Bean
    public IntegrationFlow deliveryInfoFlow() {
        return 
        	IntegrationFlows.from(deliveryInfoInputChannel())
				.handle(deliveryInfoGenerator)
				.split()
				.handle(deliveryPersister)
        		.get();
    }
    
	@Bean(DELIVERY_INFO_FLOW_INPUT_CHANNEL_ID)
	public MessageChannel deliveryInfoInputChannel() {
		return new PublishSubscribeChannel();
	}

}
