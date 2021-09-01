package com.obj.nc.functions.processors.deliveryInfo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.obj.nc.domain.*;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
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
				List<UUID> eventIds = new ArrayList<>(((HasEventIds) payload).getEventIds());
				infoBuilder = infoBuilder.eventIds(eventIds.toArray(new UUID[0]));
			} else {
				infoBuilder = infoBuilder.eventIds(new UUID[0]);
			}
			
			if (payload instanceof NotificationIntent) {
				List<UUID> intentIds = Arrays.asList(((NotificationIntent) payload).getId());
				infoBuilder = infoBuilder.intentIds(intentIds.toArray(new UUID[0]));
			} else {
				infoBuilder = infoBuilder.intentIds(new UUID[0]);
			}
			
			if (payload instanceof Message<?>) {
				List<UUID> messageIds = Arrays.asList(((Message<?>) payload).getId());
				infoBuilder = infoBuilder.messageIds(messageIds.toArray(new UUID[0]));
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
