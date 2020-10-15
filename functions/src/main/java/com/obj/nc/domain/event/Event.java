package com.obj.nc.domain.event;

import com.obj.nc.domain.BasePayload;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class Event extends BasePayload {
	
	public static final String JSON_TYPE_IDENTIFIER = "EVENT";

	
	public Event() {
	}
	
	public static Event createWithSimpleMessage(String configurationName, String message) {
		Event event = new Event();
		event.header.setConfigurationName(configurationName);
		event.body.getMessage().setText(message);
		
		return event;
	}

	@Override
	public String getPayloadTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}
	



}
