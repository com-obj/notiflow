package com.obj.nc.domain.event;

import java.io.IOException;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Event extends BaseJSONObject{

	@NotNull
	@Include
	UUID id;
	
	Header header = new Header();
	
	Body body = new Body();

	public static Event createWithSimpleMessage(String configurationName, String message) {
		Event event = new Event();
		event.generateAndSetID();
		event.header.setConfigurationName(configurationName);
		event.body.message.setText(message);
		return event;
	}
	
	private void generateAndSetID() {
		id = generateUUID();
	}
	
	public void regenerateAndSetID() {
		id = generateUUID();
	}

	public static Event fromJSON(JSONObject jo) {
		return fromJSON(jo.toString());
	}

	public static Event fromJSON(String jsonString) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();

			Event event = objectMapper.readValue(jsonString, Event.class);
			event.generateAndSetID();
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
