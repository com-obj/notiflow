package com.obj.nc.domain.event;

import java.util.UUID;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class EventReceiverResponse {
	
	private UUID ncEventId;

	public static EventReceiverResponse from(UUID id) {
		EventReceiverResponse resp = new EventReceiverResponse();
		resp.ncEventId = id;
		return resp;
	}
}
