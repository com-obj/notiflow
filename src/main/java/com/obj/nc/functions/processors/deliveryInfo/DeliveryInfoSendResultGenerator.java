package com.obj.nc.functions.processors.deliveryInfo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.obj.nc.domain.HasEventIds;
import com.obj.nc.domain.HasPreviousIntentIds;
import com.obj.nc.domain.HasPreviousMessageIds;
import com.obj.nc.domain.HasRecievingEndpoints;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.functions.processors.senders.dtos.DeliveryInfoSendResult;

import com.obj.nc.functions.processors.senders.dtos.DeliveryInfoSendResult.DeliveryInfoSendResultBuilder;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class DeliveryInfoSendResultGenerator extends ProcessorFunctionAdapter<HasRecievingEndpoints, List<DeliveryInfoSendResult>> {
	
	private final DELIVERY_STATUS status;
	
	@Override
	protected List<DeliveryInfoSendResult> execute(HasRecievingEndpoints payload) {
		List<DeliveryInfoSendResult> results= new ArrayList<>();
		
		for (RecievingEndpoint endpoint: payload.getRecievingEndpoints()) {
			DeliveryInfoSendResultBuilder infoBuilder = DeliveryInfoSendResult.builder()
					.status(status)
					.recievingEndpoint(endpoint)
					.processedOn(Instant.now());
					
			if (payload instanceof HasEventIds) {
				UUID[] eventIds = ((HasEventIds) payload).getEventIds().toArray(new UUID[0]);
				infoBuilder = infoBuilder.eventIds(eventIds);
			} else {
				infoBuilder = infoBuilder.eventIds(new UUID[0]);
			}
			
			if (payload instanceof HasPreviousIntentIds) {
				UUID[] intentIds = ((HasPreviousIntentIds) payload).getPreviousIntentIds().toArray(new UUID[0]);
				infoBuilder = infoBuilder.intentIds(intentIds);
			} else {
				infoBuilder = infoBuilder.intentIds(new UUID[0]);
			}
			
			if (payload instanceof HasPreviousMessageIds) {
				UUID[] messageIds = ((HasPreviousMessageIds) payload).getPreviousMessageIds().toArray(new UUID[0]);
				infoBuilder = infoBuilder.messageIds(messageIds);
			} else {
				infoBuilder = infoBuilder.messageIds(new UUID[0]);
			}
			
			DeliveryInfoSendResult info = infoBuilder.build();
			adapt(info);
			results.add(info);
		}
		
		return results;
	}
	
	protected void adapt(DeliveryInfoSendResult info){
		//modify, add data in subtypes if needed
	}
	

}
