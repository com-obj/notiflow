package com.obj.nc.domain.notifIntent;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode
public class IntentReceiverResponse {
	
	private UUID ncIntentId;

	public static IntentReceiverResponse from(UUID id) {
		IntentReceiverResponse resp = new IntentReceiverResponse();
		resp.ncIntentId = id;
		return resp;
	}
	
}
