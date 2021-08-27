package com.obj.nc.functions.processors.deliveryInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.obj.nc.domain.HasIntentIds;
import com.obj.nc.domain.HasMessageIds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.HasEventIds;
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
		
		Message<?> failedMsg = failedPayloadExtractor.apply(failedPayload);
		Object payload = failedMsg.getPayload();
		
		List<? extends RecievingEndpoint> endpoints = extracteEndpoints(payload);
		List<UUID> eventIds = extractEventIds(payload);
		List<UUID> intentIds = extractIntentIds(payload);
		List<UUID> messageIds = extractMessageIds(payload);
		
		List<DeliveryInfo> results= new ArrayList<>();		
		
		for (RecievingEndpoint endpoint: endpoints) {
			
			for (UUID eventId: eventIds) {
				DeliveryInfo info = DeliveryInfo.builder()
						.endpointId(endpoint.getId())
						.eventId(eventId)
						.status(DELIVERY_STATUS.FAILED)
						.failedPayloadId(failedPayload.getId())
						.build();
				
				results.add(info);
			}
			
			for (UUID intentId: intentIds) {
				DeliveryInfo info = DeliveryInfo.builder()
						.endpointId(endpoint.getId())
						.intentId(intentId)
						.status(DELIVERY_STATUS.FAILED)
						.failedPayloadId(failedPayload.getId())
						.build();
				
				results.add(info);
			}
			
			for (UUID messageId: messageIds) {
				DeliveryInfo info = DeliveryInfo.builder()
						.endpointId(endpoint.getId())
						.messageId(messageId)
						.status(DELIVERY_STATUS.FAILED)
						.failedPayloadId(failedPayload.getId())
						.build();
				
				results.add(info);
			}
			
		}
		
		return results;
	}

	private List<UUID> extractEventIds(Object payload) {
		List<UUID> evetIds = new ArrayList<>();
		if (payload instanceof HasEventIds) {
			evetIds = ((HasEventIds)payload).getEventIds();
		} else {
			log.debug("Cannot extract source event IDs from message becaase payload is not of type HasEventIds. Its of type {}", payload.getClass());
		}
		return evetIds;
	}
	
	private List<UUID> extractIntentIds(Object payload) {
		List<UUID> intentIds = new ArrayList<>();
		if (payload instanceof HasIntentIds) {
			intentIds = ((HasIntentIds)payload).getIntentIds();
		} else {
			log.debug("Cannot extract source intent IDs from message becaase payload is not of type HasIntentIds. Its of type {}", payload.getClass());
		}
		return intentIds;
	}
	
	private List<UUID> extractMessageIds(Object payload) {
		List<UUID> messageIds = new ArrayList<>();
		if (payload instanceof HasMessageIds) {
			messageIds = ((HasMessageIds)payload).getMessageIds();
		} else {
			log.debug("Cannot extract source message IDs from message becaase payload is not of type HasMessageIds. Its of type {}", payload.getClass());
		}
		return messageIds;
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
