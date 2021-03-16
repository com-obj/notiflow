package com.obj.nc.domain.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.BasePayload;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = false)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class Event extends BasePayload {
	
	public static final String JSON_TYPE_IDENTIFIER = "EVENT";

	
	public Event() {
	}
	
	public static Event createWithSimpleMessage(String flowId, String message) {
		Event event = new Event();
		event.header.setFlowId(flowId);
		event.body.getMessage().getContent().setText(message);
		
		return event;
	}

	@Override
	@JsonIgnore
	public String getPayloadTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}
	
}
