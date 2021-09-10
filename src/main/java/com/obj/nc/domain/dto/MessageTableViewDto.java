package com.obj.nc.domain.dto;

import com.obj.nc.domain.message.MessagePersistentState;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class MessageTableViewDto {
    
    private UUID id;
    private Instant timeCreated;
    private UUID[] endpointIds;
    private UUID[] previousEventIds;
    private UUID[] previousIntentIds;
    private UUID[] previousMessageIds;
    
    public static MessageTableViewDto from(MessagePersistentState messagePersistentState) {
        return MessageTableViewDto
                .builder()
                .id(messagePersistentState.getId())
                .timeCreated(messagePersistentState.getTimeCreated())
                .endpointIds(messagePersistentState.getEndpointIds())
                .previousEventIds(messagePersistentState.getPreviousEventIds())
                .previousIntentIds(messagePersistentState.getPreviousIntentIds())
                .previousMessageIds(messagePersistentState.getPreviousMessageIds())
                .build();
    }
    
}