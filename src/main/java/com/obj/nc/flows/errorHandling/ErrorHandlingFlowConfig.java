package com.obj.nc.flows.errorHandling;

import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.DELIVERY_INFO_FAILED_FLOW_INPUT_CHANNEL_ID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

import com.obj.nc.functions.processors.errorHandling.SpringMessageToFailedPaylodFunction;
import com.obj.nc.functions.sink.failedPaylodPersister.FailedPayloadPersister;

import lombok.extern.log4j.Log4j2;

/**
 * One has to make sure that error in errorHandlingFlow doesn't cuase infinite loop. Currently this is achieved by errorChannel not being async and thus 
 * sending message to this channel wit errors will result in error propagation to the caller. If any step in the error processing chain would be 
 * async, spring would put failed message from error handling flow back to the errorChannel
 * @return
 */
@Configuration
@Log4j2
public class ErrorHandlingFlowConfig {
	
	public static final String ERROR_CHANNEL_NAME = "errorChannel";
	
	//Default channel for errorMessages used by spring
	@Qualifier(ERROR_CHANNEL_NAME)
	@Autowired private PublishSubscribeChannel errorChannel;
	@Autowired private SpringMessageToFailedPaylodFunction failedPaylodTranformer;
	@Autowired private FailedPayloadPersister failedPaylodPersister;
	
	
    @Bean
    public IntegrationFlow errorPayloadRecievedFlowConfig() {
        return 
        	IntegrationFlows.from(errorChannel)
				.handle(failedPaylodTranformer)
				.wireTap( flowConfig -> 
					flowConfig.channel(DELIVERY_INFO_FAILED_FLOW_INPUT_CHANNEL_ID)
				)
				.handle(failedPaylodPersister)
        		.get();
    }

}
