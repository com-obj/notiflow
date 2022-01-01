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

import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.senders.dtos.DeliveryInfoSendResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
public class DeliveryInfoSendTransformer extends ProcessorFunctionAdapter<DeliveryInfoSendResult, List<DeliveryInfo>> {

	@Override
	protected List<DeliveryInfo> execute(DeliveryInfoSendResult deliveryInfo) {
		List<DeliveryInfo> infos = createFromSendResults(deliveryInfo);
				
		infos.forEach(info -> info.setId(UUID.randomUUID()));
		
		return infos;
	}

	private List<DeliveryInfo> createFromSendResults(DeliveryInfoSendResult sendResult) {
		List<DeliveryInfo> resultInfos = new ArrayList<>();

		for (UUID messageId: sendResult.getMessageIds()) {
			DeliveryInfo info = DeliveryInfo.builder()
					.endpointId(sendResult.getReceivingEndpoint().getId())
					.messageId(messageId)
					.status(sendResult.getStatus())
					.build();
			resultInfos.add(info);
		}
		
		return resultInfos;
	}
}
