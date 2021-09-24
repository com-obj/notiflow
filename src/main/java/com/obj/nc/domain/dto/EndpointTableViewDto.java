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

package com.obj.nc.domain.dto;

import java.util.UUID;

import com.obj.nc.domain.endpoints.ReceivingEndpointWithStats;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EndpointTableViewDto {
    
    private final UUID id;
    private final String name;
    private final EndpointType type;
    private long eventsCount;
    private long intentsCount;
    private long messagesCount;
    private long endpointsCount;
    private long messagesSentCount;
    private long messagesReadCount;
    private long messagesFailedCount;
    
    public enum EndpointType {
        EMAIL, SMS, MAILCHIMP
    }
    
    public static EndpointTableViewDto from(ReceivingEndpointWithStats endpointWithStats) {
        return EndpointTableViewDto
                .builder()
                .id(endpointWithStats.getEndpoint().getId())
                .name(endpointWithStats.getEndpoint().getEndpointId())
                .type(EndpointType.valueOf(endpointWithStats.getEndpoint().getEndpointType()))
                .eventsCount(endpointWithStats.getStats().getEventsCount())
                .intentsCount(endpointWithStats.getStats().getIntentsCount())
                .messagesCount(endpointWithStats.getStats().getMessagesCount())
                .endpointsCount(endpointWithStats.getStats().getEndpointsCount())
                .messagesSentCount(endpointWithStats.getStats().getMessagesSentCount())
                .messagesReadCount(endpointWithStats.getStats().getMessagesReadCount())
                .messagesFailedCount(endpointWithStats.getStats().getMessagesFailedCount())
                .build();
    }
    
}
