package com.obj.nc.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
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
	private UUID id;
	
	@NotNull
	@Include
	private List<UUID> eventIds = new ArrayList<>();
	
	protected ProcessingInfo processingInfo;
	
//	public ProcessingInfo stepStart(String processingStepName, Object startPayload) {
//	    log.info("Generating processing info for step {}", processingStepName);
//	    
//		ProcessingInfo processingInfo = new ProcessingInfo();
//		processingInfo.initProcessingInfoOnStepStart(processingStepName, this, startPayload);
//		setProcessingInfo(processingInfo);
//
//		return processingInfo;
//	}

//	public void stepFinish(Object startPayload) {
//		getProcessingInfo().stepFinish(this, startPayload);
//		
//	}

	public void generateAndSetID() {
		id = generateUUID();
	}

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

		merged.flowId = flowId;
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
