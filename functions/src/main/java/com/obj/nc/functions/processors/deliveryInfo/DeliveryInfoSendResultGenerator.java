package com.obj.nc.functions.processors.deliveryInfo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.obj.nc.domain.HasEventIds;
import com.obj.nc.domain.HasRecievingEndpoints;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.functions.processors.senders.dtos.DeliveryInfoSendResult;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class DeliveryInfoSendResultGenerator extends ProcessorFunctionAdapter<HasRecievingEndpoints, List<DeliveryInfoSendResult>> {
	
	private final DELIVERY_STATUS status;
	
	@Override
	protected List<DeliveryInfoSendResult> execute(HasRecievingEndpoints payload) {
		List<DeliveryInfoSendResult> results= new ArrayList<>();
		
		for (RecievingEndpoint endpoint: payload.getRecievingEndpoints()) {
			UUID[] eventIds = null;
			if (payload instanceof HasEventIds) {
				eventIds = ((HasEventIds) payload).getEventIds().toArray(new UUID[0]);
			}
			
			DeliveryInfoSendResult info = DeliveryInfoSendResult.builder()
					.eventIds(eventIds)
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
