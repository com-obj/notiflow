package com.obj.nc.domain.headers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.obj.nc.domain.BaseJSONObject;
import com.obj.nc.domain.HasFlowId;
import com.obj.nc.utils.JsonUtils;

import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Log4j2
public class Header extends BaseJSONObject implements HasFlowId {
	
	@JsonProperty("flow-id")
	private String flowId;

	@NotNull
	@Include
	private List<UUID> eventIds = new ArrayList<>();
	
	protected ProcessingInfo processingInfo;
	
	public static final String SUPRESS_GENERATE_PROC_INFO_PARAM_NAME = "SUPRESS_GENERATE_PROCESSING_INFO";

	public void copyHeaderFrom(Header header) {
		if (header == null) {
			return;
		}
		
		BeanUtils.copyProperties(header, this);
	}

	public Header merge(Header other) {
		Header merged = new Header();

		merged.setAttributes(this.getAttributes());
		other.getAttributes().forEach((key, value) -> merged.getAttributes().putIfAbsent(key, value));

		merged.flowId = other.flowId;

		merged.eventIds = other.eventIds;
		merged.eventIds.addAll(other.getEventIds());

		return merged;
	}

	public String eventIdsAsJSONString() {
		return JsonUtils.writeObjectToJSONString(eventIds);
	}

	public void addEventId(UUID eventId) {
		eventIds.add(eventId);
	}
	
	public UUID[] getEventIdsAsArray() {
		return eventIds.toArray(new UUID[0]);
	}
	
	@JsonIgnore
	public boolean isSupressGenerateProcessingInfo() {
		if (hasAttribute(SUPRESS_GENERATE_PROC_INFO_PARAM_NAME)) {
			return (Boolean) getAttributeValue(SUPRESS_GENERATE_PROC_INFO_PARAM_NAME);
		}
		
		return false;
	}

}
