package com.obj.nc.functions.processors.errorHandling;

import org.springframework.messaging.Message;

import com.obj.nc.flows.errorHandling.domain.FailedPaylod;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.utils.JsonUtils;

public class SpringMessageToFailedPaylodFunction extends ProcessorFunctionAdapter<Message<?>, FailedPaylod> {
	
	@Override
	protected FailedPaylod execute(Message<?> payload) {
		FailedPaylod failedPaylod = FailedPaylod.builder()
				.payloadJson( JsonUtils.writeObjectToJSONNode(payload.getPayload()) )
				.build();
		
		return failedPaylod;
	}


}
