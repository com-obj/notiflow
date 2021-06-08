package com.obj.nc.flows.deliveryInfo;


import java.util.List;
import java.util.concurrent.Future;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

import com.obj.nc.domain.HasRecievingEndpoints;
import com.obj.nc.flows.errorHandling.ErrorHandlingFlowConfig;
import com.obj.nc.flows.errorHandling.domain.FailedPaylod;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;

import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.*;

@MessagingGateway(errorChannel = ErrorHandlingFlowConfig.ERROR_CHANNEL_NAME)
public interface DeliveryInfoFlow {

	@Gateway(requestChannel=DELIVERY_INFO_PROCESSING_FLOW_INPUT_CHANNEL_ID, replyChannel = DELIVERY_INFO_FLOW_OUTPUT_CHANNEL_ID)
    Future<List<DeliveryInfo>> createAndPersistProcessingDeliveryInfo(HasRecievingEndpoints msg);
	
	@Gateway(requestChannel=DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID, replyChannel = DELIVERY_INFO_FLOW_OUTPUT_CHANNEL_ID)
    Future<List<DeliveryInfo>> createAndPersistSentDeliveryInfo(HasRecievingEndpoints msg);

	@Gateway(requestChannel=DELIVERY_INFO_FAILED_FLOW_INPUT_CHANNEL_ID, replyChannel = DELIVERY_INFO_FLOW_OUTPUT_CHANNEL_ID)
    Future<List<DeliveryInfo>> createAndPersistFailedDeliveryInfo(FailedPaylod failedPaylod);
    
    @Gateway(requestChannel=DELIVERY_INFO_READ_FLOW_INPUT_CHANNEL_ID, replyChannel = DELIVERY_INFO_FLOW_OUTPUT_CHANNEL_ID)
    Future<List<DeliveryInfo>> createAndPersistReadDeliveryInfo(HasRecievingEndpoints msg);

}
