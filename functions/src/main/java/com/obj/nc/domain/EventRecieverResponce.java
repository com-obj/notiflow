package com.obj.nc.domain;

import java.util.UUID;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class EventRecieverResponce {
	
	private UUID ncEventId;

	public static EventRecieverResponce from(UUID id) {
		EventRecieverResponce resp = new EventRecieverResponce();
		resp.ncEventId = id;
		return resp;
	}
}
