package com.obj.nc.domain.event;

import java.io.IOException;

import org.json.JSONObject;
import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Event {
	
	Header header = new Header();
	Body body = new Body();
	
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

			return objectMapper.readValue(jsonString, Event.class);
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


	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((header == null) ? 0 : header.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Event other = (Event) obj;
		if (header == null) {
			if (other.header != null)
				return false;
		} else if (!header.equals(other.header))
			return false;
		return true;
	}

}
