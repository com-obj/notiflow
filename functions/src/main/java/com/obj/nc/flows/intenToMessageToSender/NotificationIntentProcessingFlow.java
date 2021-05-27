package com.obj.nc.flows.intenToMessageToSender;

import static com.obj.nc.flows.intenToMessageToSender.NotificationIntentProcessingFlowConfig.INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.flows.errorHandling.ErrorHandlingFlowConfig;

@MessagingGateway(errorChannel = ErrorHandlingFlowConfig.ERROR_CHANNEL_NAME)
public interface NotificationIntentProcessingFlow {

	@Gateway(requestChannel=INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID)
	public void processNotificationIntent(NotificationIntent<?> intent);
	
}
