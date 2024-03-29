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

import com.obj.nc.domain.HasPreviousMessageIds;
import com.obj.nc.domain.HasReceivingEndpoints;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.flows.errorHandling.domain.FailedPayload;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.functions.processors.errorHandling.FailedPaylodExtractor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper=false)
@Component
@Slf4j
public class DeliveryInfoFailedGenerator extends ProcessorFunctionAdapter<FailedPayload, List<DeliveryInfo>> {
	
	@Autowired
	private FailedPaylodExtractor failedPayloadExtractor; 
	
	@Override
	@SneakyThrows
	protected List<DeliveryInfo> execute(FailedPayload failedPayload) {
		
		org.springframework.messaging.Message<?> failedMsg = failedPayloadExtractor.apply(failedPayload);
		Object payload = failedMsg.getPayload();
        		
		List<DeliveryInfo> results= new ArrayList<>();
		
		List<? extends ReceivingEndpoint> endpoints = extractEndpoints(payload);
		
        if (endpoints.size() == 0) {
            log.info("Cannot extract endpoints from failed payload. Not possible to create failed delivery info for failed payload " + failedPayload );
            return results;
        }
		
        for (ReceivingEndpoint endpoint: endpoints) {
			// Only add failed delivery info for the last message, not for all previous forms of the last message
			results.add(
					failedDeliveryInfoBuilder(failedPayload, endpoint)
							.messageId(((Message<?>) payload).getId())
							.build()
			);
		}
		
		return results;
	}
	
	private DeliveryInfo.DeliveryInfoBuilder failedDeliveryInfoBuilder(FailedPayload failedPayload, ReceivingEndpoint endpoint) {
		return DeliveryInfo.builder()
				.endpointId(endpoint.getId())
				.status(DELIVERY_STATUS.FAILED)
				.failedPayloadId(failedPayload.getId());
	}
	
	private List<? extends ReceivingEndpoint> extractEndpoints(Object payload) {
		if (!(payload instanceof HasReceivingEndpoints)) {
			log.debug("Cannot generate Failed delivery infos from message because payload is not of type HasReceivingEndpoints. Its of type {}", payload.getClass());
			return new ArrayList<>();
		}

		return ((HasReceivingEndpoints)payload).getReceivingEndpoints();
	}
	

}
