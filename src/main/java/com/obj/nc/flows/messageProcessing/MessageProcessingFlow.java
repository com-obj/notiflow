package com.obj.nc.flows.messageProcessing;

import static com.obj.nc.flows.messageProcessing.MessageProcessingFlowConfig.MESSAGE_PROCESSING_FLOW_INPUT_CHANNEL_ID;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

import com.obj.nc.domain.message.Message;
import com.obj.nc.flows.errorHandling.ErrorHandlingFlowConfig;

@MessagingGateway(errorChannel = ErrorHandlingFlowConfig.ERROR_CHANNEL_NAME)
public interface MessageProcessingFlow {

	@Gateway(requestChannel=MESSAGE_PROCESSING_FLOW_INPUT_CHANNEL_ID)
	public void processMessage(Message<?> msg);
	
}
