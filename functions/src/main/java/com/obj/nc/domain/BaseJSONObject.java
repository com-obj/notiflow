package com.obj.nc.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.obj.nc.domain.event.Event;
import com.obj.nc.utils.JsonUtils;

import lombok.Data;

@Data
public class BaseJSONObject {

	private Map<String, Object> attributes = new HashMap<String, Object>();

	@JsonAnySetter
	public void setAttributeValue(String key, Object value) {
		attributes.put(key, value);
	}

	public static UUID generateUUID() {
		return UUID.randomUUID();
	}

	public String toJSONString() {
		return JsonUtils.writeObjectToJSONString(this);
	}

	public boolean containsAttributes(List<String> attributes) {
		return attributes.stream().allMatch(attr -> this.attributes.get(attr) != null);
	}

	public boolean containsNestedAttributes(List<String> attributes, String... attributePath) {
		if (attributePath.length == 0) {
			return attributes.stream().allMatch(attr -> this.attributes.get(attr) != null);
		}

		Map<String, Object> fieldValue = getAttributeValueAs(attributePath[0], Map.class);
		if (fieldValue == null) {
			return false;
		}

		for (int i = 1; i < attributePath.length; i++) {
			fieldValue = JsonUtils.readClassFromObject(fieldValue.get(attributePath[i]), Map.class);
			if (fieldValue == null) {
				return false;
			}
		}

		final Map<String, Object> finalFieldValue = fieldValue;
		return attributes.stream().allMatch(attr -> finalFieldValue.get(attr) != null);
	}

	public <T> T getAttributeValueAs(String attributeName, Class<T> clazz) {
		return JsonUtils.readClassFromObject(attributes.get(attributeName), clazz);
	}

}
