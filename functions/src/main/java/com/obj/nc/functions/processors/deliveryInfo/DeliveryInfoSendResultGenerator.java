package com.obj.nc.functions.processors.deliveryInfo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.senders.dtos.DeliveryInfoSendResult;
import com.obj.nc.functions.sink.deliveryInfoPersister.domain.DeliveryInfo.DELIVERY_STATUS;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class DeliveryInfoSendResultGenerator extends ProcessorFunctionAdapter<BasePayload<?>, List<DeliveryInfoSendResult>> {
	
	private final DELIVERY_STATUS status;
	
	//should rely on HasRecievingEndpoints and HasEventIds like DeliveryInfoFailedGenerator
	@Override
	protected List<DeliveryInfoSendResult> execute(BasePayload<?> payload) {
		List<DeliveryInfoSendResult> results= new ArrayList<>();
		
		for (RecievingEndpoint endpoint: payload.getRecievingEndpoints()) {
			DeliveryInfoSendResult info = DeliveryInfoSendResult.builder()
					.eventIds(payload.getHeader().getEventIds().toArray(new UUID[0]))
					.status(status)
					.recievingEndpoint(endpoint)
					.processedOn(Instant.now())
					.build();
			adapt(info);
			
			results.add(info);
		}
		
		return results;
	}
	
	protected void adapt(DeliveryInfoSendResult info){
		//modify, add data in subtypes if needed
	}
	

}
