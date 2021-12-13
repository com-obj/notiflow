/*
 * Copyright (C) 2021 the original author or authors.
 * This file is part of Notiflow
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.obj.nc.domain.delivery;

import com.obj.nc.domain.deliveryOptions.EndpointDeliveryOptionsConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Table("nc_message_2_endpoint_rel")
public class Message2EndpointRelation implements Persistable<UUID> {
    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @CreatedDate
    private Instant timeCreated;

    private UUID messageId;
    private UUID endpointId;

//    @Column("delivery_options")
    @Transient
    private EndpointDeliveryOptionsConfig deliveryOptions;

    @Override
    public boolean isNew() {
        return timeCreated == null;
    }
}
