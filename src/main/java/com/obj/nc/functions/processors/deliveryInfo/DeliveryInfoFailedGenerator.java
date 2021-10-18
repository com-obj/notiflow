/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.functions.processors.deliveryInfo;

import com.obj.nc.domain.HasPreviousEventIds;
import com.obj.nc.domain.HasPreviousIntentIds;
import com.obj.nc.domain.HasPreviousMessageIds;
import com.obj.nc.domain.HasReceivingEndpoints;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.flows.errorHandling.domain.FailedPayload;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.functions.processors.errorHandling.FailedPaylodExtractor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper=false)
@Component
@Log4j2
public class DeliveryInfoFailedGenerator extends ProcessorFunctionAdapter<FailedPayload, List<DeliveryInfo>> {
	
	@Autowired
	private FailedPaylodExtractor failedPayloadExtractor; 
	
	@Override
	@SneakyThrows
	protected List<DeliveryInfo> execute(FailedPayload failedPayload) {
		
		org.springframework.messaging.Message<?> failedMsg = failedPayloadExtractor.apply(failedPayload);
		Object payload = failedMsg.getPayload();
        		
		List<DeliveryInfo> results= new ArrayList<>();
		
		List<? extends ReceivingEndpoint> endpoints = extracteEndpoints(payload);
		
        if (endpoints.size() == 0) {
            log.info("Cannot extract endpoints from fayled payload. Not possible to create failed delivery infos for failed paylod " + failedPayload );
            return results;
        }
		
        //TODO: should we duplicate the deliveryInfosHere for each dimension? Maybe we should introduce LIst<UUID> for intentId, eventId, messageId
        //or should there be eventId, intentId at all? is it not that it makes sence to only calculate Delivery info for message and endpoint?
        for (ReceivingEndpoint endpoint: endpoints) {
			
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
	
	private DeliveryInfo.DeliveryInfoBuilder failedDeliveryInfoBuilder(FailedPayload failedPayload, ReceivingEndpoint endpoint) {
		DeliveryInfo.DeliveryInfoBuilder infoBuilder = DeliveryInfo.builder()
				.endpointId(endpoint.getId())
				.status(DELIVERY_STATUS.FAILED)
				.failedPayloadId(failedPayload.getId());
		return infoBuilder;
	}
	
	private List<? extends ReceivingEndpoint> extracteEndpoints(Object payload) {
		if (!(payload instanceof HasReceivingEndpoints)) {
			log.debug("Cannot generate Failed delivery infos from message because payload is not of type HasReceivingEndpoints. Its of type {}", payload.getClass());
			return new ArrayList<>();
		}
		List<? extends ReceivingEndpoint> endpoints = ((HasReceivingEndpoints)payload).getReceivingEndpoints();
		return endpoints;
	}
	

}
