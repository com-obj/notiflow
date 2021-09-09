package com.obj.nc.domain.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenericEventStats {
	
	private long eventsCount;
	private long intentsCount;
	private long messagesCount;
	private long endpointsCount;
	private long messagesSentCount;
	private long messagesReadCount;
	private long messagesFailedCount;
	
}