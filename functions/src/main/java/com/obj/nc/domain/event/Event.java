package com.obj.nc.domain.event;

import java.io.IOException;
import java.util.LinkedList;

import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class Event extends BaseJSONObject{

	private Header header = new Header();
	private Body body = new Body();
	private ProcessingInfo processingInfo;
	
	public Event() {
	}
	
	public ProcessingInfo stepStart(String processingStepName) {
		ProcessingInfo processingInfo = new ProcessingInfo();
		processingInfo.stepStart(processingStepName, this);
		this.processingInfo = processingInfo;
		
		return this.processingInfo;
	}
	
	public void stepFinish() {
		this.processingInfo.stepFinish(this);
	}

	public static Event createWithSimpleMessage(String configurationName, String message) {
		Event event = new Event();
		event.header.setConfigurationName(configurationName);
		event.body.message.setText(message);
		
		return event;
	}
	
	public static Event fromJSON(JSONObject jo) {
		return fromJSON(jo.toString());
	}

	public static Event fromJSON(String jsonString) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();

			Event event = objectMapper.readValue(jsonString, Event.class);
			return event;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public String toJSONString() {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = objectMapper.writeValueAsString(this);

			return jsonString;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}


}
