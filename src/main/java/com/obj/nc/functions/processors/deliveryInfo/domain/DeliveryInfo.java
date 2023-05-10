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

package com.obj.nc.functions.processors.deliveryInfo.domain;

import com.obj.nc.domain.refIntegrity.Reference;
import com.obj.nc.repositories.EndpointsRepository;
import com.obj.nc.repositories.FailedPayloadRepository;
import com.obj.nc.repositories.MessageRepository;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Table("nc_delivery_info")
@Builder(toBuilder=true)
public class DeliveryInfo implements Persistable<UUID> {
	
	//Order of elements in this enum is important. Final state should be last
	public enum DELIVERY_STATUS {
		PROCESSING,
		SENT,
		FAILED,
		DISCARDED,
		DELIVERY_FAILED,
		DELIVERY_UNKNOWN,
		DELIVERY_PENDING,
		DELIVERED,
		READ
	}

	@Id
	private UUID id;
	
	@Version
	private Integer version;
	
	@NotNull
	private DELIVERY_STATUS status;
	
	@CreatedDate
	@Getter
	private Instant processedOn;
	
	@Reference(MessageRepository.class)
	private UUID messageId;
	
	@NotNull
	@Reference(EndpointsRepository.class)
	private UUID endpointId;
	
	@Reference(FailedPayloadRepository.class)
	private UUID failedPayloadId;

	private String additionalInformation;

	@Override
	public boolean isNew() {
		return processedOn == null;
	}

    public static DeliveryInfo createDiscardedDeliveryInfo(UUID payloadId, UUID endpointId) {
        return DeliveryInfo.builder()
                .id(UUID.randomUUID())
                .endpointId(endpointId)
                .messageId(payloadId)
                .status(DeliveryInfo.DELIVERY_STATUS.DISCARDED).build();
    }
	
}
