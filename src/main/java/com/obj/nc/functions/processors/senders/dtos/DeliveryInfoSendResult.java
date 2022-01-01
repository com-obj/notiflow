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

package com.obj.nc.functions.processors.senders.dtos;

import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.headers.HasHeader;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
 
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder=true)
public class DeliveryInfoSendResult implements HasHeader {
	 	
	@NotNull
	private DELIVERY_STATUS status;
	
	@NotNull
	private Instant processedOn;
	
	@NotNull
	private UUID[] messageIds;
	
	@NotNull
	private ReceivingEndpoint receivingEndpoint;

	private Header header;

	public Header getHeader() {
		if (header==null) {
			header = new Header();
		}
		return header;
	}
}
