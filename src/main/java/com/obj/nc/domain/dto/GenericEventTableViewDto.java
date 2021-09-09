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
