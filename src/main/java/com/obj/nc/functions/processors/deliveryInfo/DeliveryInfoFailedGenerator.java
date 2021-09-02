package com.obj.nc.functions.processors.deliveryInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.obj.nc.domain.HasPreviousIntentIds;
import com.obj.nc.domain.HasPreviousMessageIds;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.HasPreviousEventIds;
import com.obj.nc.domain.HasRecievingEndpoints;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.flows.errorHandling.domain.FailedPaylod;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.functions.processors.errorHandling.FailedPaylodExtractor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Data
@EqualsAndHashCode(callSuper=false)
@Component
@Log4j2
public class DeliveryInfoFailedGenerator extends ProcessorFunctionAdapter<FailedPaylod, List<DeliveryInfo>> {
	
	@Autowired
	private FailedPaylodExtractor failedPayloadExtractor; 
	
	@Override
	@SneakyThrows
	protected List<DeliveryInfo> execute(FailedPaylod failedPayload) {
		
		org.springframework.messaging.Message<?> failedMsg = failedPayloadExtractor.apply(failedPayload);
		Object payload = failedMsg.getPayload();
		
		List<? extends RecievingEndpoint> endpoints = extracteEndpoints(payload);
		
		List<DeliveryInfo> results= new ArrayList<>();		
		
		for (RecievingEndpoint endpoint: endpoints) {
			
			if (payload instanceof HasPreviousEventIds) {
				List<UUID> eventIds = ((HasPreviousEventIds) payload).getPreviousEventIds();
				eventIds.forEach(eventId -> 
						results.add(failedDeliveryInfoBuilder(failedPayload, endpoint).eventId(eventId).build()));
			}
			
			if (payload instanceof HasPreviousIntentIds) {
				List<UUID> intentIds = ((HasPreviousIntentIds) payload).getPreviousIntentIds();
				
				if (payload instanceof NotificationIntent) {
					intentIds.add(((NotificationIntent) payload).getId());
				}
				
				intentIds.forEach(intentId ->
						results.add(failedDeliveryInfoBuilder(failedPayload, endpoint).intentId(intentId).build()));
			}
			
			if (payload instanceof HasPreviousMessageIds) {
				List<UUID> messageIds = ((HasPreviousMessageIds) payload).getPreviousMessageIds();
				
				if (payload instanceof Message<?>) {
					messageIds.add(((Message<?>) payload).getId());
				}
				
				messageIds.forEach(messageId ->
						results.add(failedDeliveryInfoBuilder(failedPayload, endpoint).messageId(messageId).build()));
			}
			
		}
		
		return results;
	}
	
	private DeliveryInfo.DeliveryInfoBuilder failedDeliveryInfoBuilder(FailedPaylod failedPayload, RecievingEndpoint endpoint) {
		DeliveryInfo.DeliveryInfoBuilder infoBuilder = DeliveryInfo.builder()
				.endpointId(endpoint.getId())
				.status(DELIVERY_STATUS.FAILED)
				.failedPayloadId(failedPayload.getId());
		return infoBuilder;
	}
	
	private List<? extends RecievingEndpoint> extracteEndpoints(Object payload) {
		if (!(payload instanceof HasRecievingEndpoints)) {
			log.debug("Cannot generate Failed delivery infos from message because payload is not of type HasRecievingEndpoints. Its of type {}", payload.getClass());
			return new ArrayList<>();
		}
		List<? extends RecievingEndpoint> endpoints = ((HasRecievingEndpoints)payload).getRecievingEndpoints();
		return endpoints;
	}
	

}
