package com.obj.nc.domain.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode
public class MessageReceiverResponse {
	
	private UUID ncMessageId;

	public static MessageReceiverResponse from(UUID id) {
		MessageReceiverResponse resp = new MessageReceiverResponse();
		resp.ncMessageId = id;
		return resp;
	}
}
