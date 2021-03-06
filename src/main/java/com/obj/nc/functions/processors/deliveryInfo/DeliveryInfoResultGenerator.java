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

import com.obj.nc.domain.HasReceivingEndpoints;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.functions.processors.senders.dtos.DeliveryInfoSendResult;
import com.obj.nc.functions.processors.senders.dtos.DeliveryInfoSendResult.DeliveryInfoSendResultBuilder;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.*;

@AllArgsConstructor
public abstract class DeliveryInfoResultGenerator extends ProcessorFunctionAdapter<HasReceivingEndpoints, List<DeliveryInfoSendResult>> {
	
	private final DELIVERY_STATUS status;
	
	@Override
	protected List<DeliveryInfoSendResult> execute(HasReceivingEndpoints payload) {
		List<DeliveryInfoSendResult> results = new ArrayList<>();
		
		for (ReceivingEndpoint endpoint: payload.getReceivingEndpoints()) {
			DeliveryInfoSendResultBuilder infoBuilder = DeliveryInfoSendResult.builder()
					.status(status)
					.receivingEndpoint(endpoint)
					.processedOn(Instant.now());
			
			if (payload instanceof Message<?>) {
				List<UUID> messageIds = Collections.singletonList(((Message<?>) payload).getId());
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
