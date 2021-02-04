package com.obj.nc.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.obj.nc.utils.JsonUtils;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Header extends BaseJSONObject {
	
	@JsonProperty("configuration-name")
	private String configurationName;
		
	@NotNull
	@Include
	private UUID id;
	
	@NotNull
	@Include
	private List<UUID> eventIds = new ArrayList<>();

	public void generateAndSetID() {
		id = generateUUID();
	}

	public void copyHeaderFrom(Header header) {
		BeanUtils.copyProperties(header, this);
	}

	public Header merge(Header other) {
		Header merged = new Header();

		merged.setAttributes(this.getAttributes());
		other.getAttributes().forEach((key, value) -> merged.getAttributes().putIfAbsent(key, value));

		merged.configurationName = configurationName;
		merged.generateAndSetID();

		merged.eventIds = eventIds;
		merged.eventIds.addAll(other.getEventIds());

		return merged;
	}

	public String eventIdsAsJSONString() {
		return JsonUtils.writeObjectToJSONString(eventIds);
	}

	public void addEventId(UUID eventId) {
		eventIds.add(eventId);
	}

}
