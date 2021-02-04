package com.obj.nc.domain;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.domain.event.Event;
import com.obj.nc.utils.JsonUtils;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class BaseJSONObject {

	private Map<String, Object> attributes = new HashMap<String, Object>();

	@JsonAnySetter
	public void pubAttributeValue(String key, Object value) {
		attributes.put(key, value);
	}

	public static UUID generateUUID() {
		return UUID.randomUUID();
	}

	public static Event fromJSON(JSONObject jo) {
		return fromJSON(jo.toString());
	}

	public static Event fromJSON(String jsonString) {
		return JsonUtils.readObjectFromJSONString(jsonString, Event.class);
	}

	public String toJSONString() {
		return JsonUtils.writeObjectToJSONString(this);
	}

}
