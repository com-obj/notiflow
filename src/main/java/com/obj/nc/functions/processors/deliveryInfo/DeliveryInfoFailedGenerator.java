package com.obj.nc.functions.processors.deliveryInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.config.SpringIntegration;
import com.obj.nc.domain.HasEventIds;
import com.obj.nc.domain.HasRecievingEndpoints;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.flows.errorHandling.domain.FailedPaylod;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.errorHandling.FailedPaylodExtractor;
import com.obj.nc.functions.sink.deliveryInfoPersister.domain.DeliveryInfo;
import com.obj.nc.functions.sink.deliveryInfoPersister.domain.DeliveryInfo.DELIVERY_STATUS;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Data
@EqualsAndHashCode(callSuper=false)
@Component
@Log4j2
public class DeliveryInfoFailedGenerator extends ProcessorFunctionAdapter<FailedPaylod, List<DeliveryInfo>> {
	
	@Autowired @Qualifier(SpringIntegration.OBJECT_MAPPER_FOR_MESSAGES_BEAN_NAME)
	private ObjectMapper jsonConverterForMessages;
	@Autowired
	private FailedPaylodExtractor failedPayloadExtractor; 
	
	@Override
	@SneakyThrows
	protected List<DeliveryInfo> execute(FailedPaylod failedPayload) {
		
		Message<?> failedMsg = failedPayloadExtractor.apply(failedPayload);
		Object payload = failedMsg.getPayload();
		
		List<RecievingEndpoint> endpoints = extracteEndpoints(payload);
		List<UUID> eventIds = extractEventIds(payload);
		
		
		List<DeliveryInfo> results= new ArrayList<>();		
		
		for (RecievingEndpoint endpoint: endpoints) {
			
			for (UUID eventId: eventIds) {
				DeliveryInfo info = DeliveryInfo.builder()
						.endpointId(endpoint.getEndpointId())
						.eventId(eventId)
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

	private List<RecievingEndpoint> extracteEndpoints(Object payload) {
		if (!(payload instanceof HasRecievingEndpoints)) {
			log.debug("Cannot generate Failed delivery infos from message because payload is not of type HasRecievingEndpoints. Its of type {}", payload.getClass());
			return new ArrayList<>();
		}
		List<RecievingEndpoint> endpoints = ((HasRecievingEndpoints)payload).getRecievingEndpoints();
		return endpoints;
	}
	

}
