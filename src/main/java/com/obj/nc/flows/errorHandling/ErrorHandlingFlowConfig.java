package com.obj.nc.flows.errorHandling;

import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.DELIVERY_INFO_FAILED_FLOW_INPUT_CHANNEL_ID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

import com.obj.nc.functions.processors.errorHandling.SpringMessageToFailedPayloadFunction;
import com.obj.nc.functions.sink.failedPaylodPersister.FailedPayloadPersister;


/**
 * One has to make sure that error in errorHandlingFlow doesn't cause infinite loop. Currently this is achieved by errorChannel not being async and thus 
 * sending message to this channel with errors will result in error propagation to the caller. If any step in the error processing chain would be 
 * async, spring would put failed message from error handling flow back to the errorChannel
 * @return
 */
@Configuration
public class ErrorHandlingFlowConfig {
	
	public static final String ERROR_CHANNEL_NAME = "errorChannel";
	
	//Default channel for errorMessages used by spring
	@Qualifier(ERROR_CHANNEL_NAME)
	@Autowired private PublishSubscribeChannel errorChannel;
	@Autowired private SpringMessageToFailedPayloadFunction failedPayloadTransformer;
	@Autowired private FailedPayloadPersister failedPayloadPersister;
	
	
    @Bean
    public IntegrationFlow errorPayloadReceivedFlowConfig() {
        return 
        	IntegrationFlows.from(errorChannel)
				.handle(failedPayloadTransformer)
				.wireTap( flowConfig -> 
					flowConfig.channel(DELIVERY_INFO_FAILED_FLOW_INPUT_CHANNEL_ID)
				)
				.handle(failedPayloadPersister)
        		.get();
    }

}
