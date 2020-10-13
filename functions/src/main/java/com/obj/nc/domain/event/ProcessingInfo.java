package com.obj.nc.domain.event;

import java.time.Instant;
import java.util.LinkedList;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.utils.DiffMatchPatch;
import com.obj.nc.utils.DiffMatchPatch.Diff;

import lombok.Data;
import lombok.EqualsAndHashCode.Include;

@Data
public class ProcessingInfo {
	@NotNull
	@Include
	private UUID processingId;
	
	private UUID prevProcessingId;
	
	private String stepName;
	private Integer stepIndex;
	
	private Instant timeStampStart;
	private Instant timeStampFinish;
	private long durationInMs;
	
	@Transient
	private String eventJson;
	@Transient
	private String modifiedEventJson;
	@Transient
	private String diffJson;

	
	public void stepStart(String processingStepName, Event event) {
		this.eventJson = event.toJSONString();
		
		ProcessingInfo prevProcessingInfo = event.getProcessingInfo();
		this.prevProcessingId = prevProcessingInfo!=null? prevProcessingInfo.getProcessingId(): null;
		
		this.processingId =  BaseJSONObject.generateUUID();
		
		this.stepName = processingStepName;
		this.stepIndex = prevProcessingInfo!=null? prevProcessingInfo.getStepIndex()+1: 0;

		Instant now = Instant.now();
		timeStampStart = now;
		timeStampFinish = now;
	}
	
	public void stepFinish(Event event) {
		timeStampFinish = Instant.now();
		modifiedEventJson = event.toJSONString();
		
		calculateDiffToPreviosVersion();
	}
	
	private void calculateDiffToPreviosVersion() {
		try {	
			DiffMatchPatch diff = new DiffMatchPatch();
			LinkedList<Diff> diffs = diff.diff_main(eventJson, modifiedEventJson);
			
			ObjectMapper objectMapper = new ObjectMapper();
			diffJson = objectMapper.writeValueAsString(diffs.toArray());
			
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
}