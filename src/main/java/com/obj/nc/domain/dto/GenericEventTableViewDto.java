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

import com.obj.nc.domain.event.GenericEventWithStats;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class GenericEventTableViewDto {
    
    private UUID id;
    private String externalId;
    private String flowId;
    private Instant timeCreated;
    private Instant timeConsumed;
    private long eventsCount;
    private long intentsCount;
    private long messagesCount;
    private long endpointsCount;
    private long messagesSentCount;
    private long messagesReadCount;
    private long messagesFailedCount;
    
    public static GenericEventTableViewDto from(GenericEventWithStats genericEventWithStats) {
        return GenericEventTableViewDto
                .builder()
                .id(genericEventWithStats.getEvent().getId())
                .externalId(genericEventWithStats.getEvent().getExternalId())
                .flowId(genericEventWithStats.getEvent().getFlowId())
                .timeCreated(genericEventWithStats.getEvent().getTimeCreated())
                .timeConsumed(genericEventWithStats.getEvent().getTimeConsumed())
                .eventsCount(genericEventWithStats.getStats().getEventsCount())
                .intentsCount(genericEventWithStats.getStats().getIntentsCount())
                .messagesCount(genericEventWithStats.getStats().getMessagesCount())
                .endpointsCount(genericEventWithStats.getStats().getEndpointsCount())
                .messagesSentCount(genericEventWithStats.getStats().getMessagesSentCount())
                .messagesReadCount(genericEventWithStats.getStats().getMessagesReadCount())
                .messagesFailedCount(genericEventWithStats.getStats().getMessagesFailedCount())
                .build();
    }   
    
}
