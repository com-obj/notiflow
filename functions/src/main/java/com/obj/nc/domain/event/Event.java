package com.obj.nc.domain.event;

import java.io.IOException;
import java.util.LinkedList;

import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.domain.BaseJSONObject;
import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.Body;
import com.obj.nc.domain.Header;
import com.obj.nc.domain.ProcessingInfo;
import com.obj.nc.utils.DiffMatchPatch;
import com.obj.nc.utils.DiffMatchPatch.Diff;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Event extends BasePayload {

	private Header header = new Header();
	private Body body = new Body();

	
	public Event() {
	}
	
	public static Event createWithSimpleMessage(String configurationName, String message) {
		Event event = new Event();
		event.header.setConfigurationName(configurationName);
		event.body.getMessage().setText(message);
		
		return event;
	}
	



}
