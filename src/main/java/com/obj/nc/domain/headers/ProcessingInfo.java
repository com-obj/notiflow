/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.domain.headers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.BaseJSONObject;
import com.obj.nc.domain.HasEventId;
import com.obj.nc.domain.HasPreviousEventIds;
import com.obj.nc.domain.refIntegrity.Reference;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Builder
@EqualsAndHashCode(of = "processingId")
@Table("nc_processing_info")
@ToString
public class ProcessingInfo implements Persistable<UUID> {
	@NotNull
	@Id
	private UUID processingId;
	
	@Version
	private Integer version;
	
	@NotNull
	//@Reference(ProcessingInfoRepository.class)
	//Processing info are persisted in async manner. The invalid reference might be result of wrong order, not wrong processing
	//in future we might implement something like deferred which would postpone the check to later evaluation
	private UUID prevProcessingId;
	
	@NotNull
	private String stepName;
	@NotNull
	private Integer stepIndex;
	
	@NotNull
	private Instant timeProcessingStart;
	@NotNull
	private Instant timeProcessingEnd;
	@NotNull
	private long stepDurationMs;
	
	@NotEmpty
	@Reference(GenericEventRepository.class)
	@Builder.Default
	private UUID[] eventIds = new UUID[0];
	
	@JsonIgnore
	private String payloadJsonStart;

	@JsonIgnore
	private String payloadJsonEnd;
	
	@JsonIgnore
	@Transient
	private String diffJson;
	
	public static ProcessingInfo createCopy(ProcessingInfo info) {
		ProcessingInfo newPi = new ProcessingInfo();
		BeanUtils.copyProperties(info, newPi);
		return newPi;
	}
	
	public static ProcessingInfo createProcessingInfoOnStepStart(String processingStepName, ProcessingInfo prevProcessingInfo, Object startPayload) {
		log.debug("Generating start processing info for step {}", processingStepName);
		
		ProcessingInfo stepProcessinfInfo = new ProcessingInfo();
		stepProcessinfInfo.payloadJsonStart = startPayload!=null?JsonUtils.writeObjectToJSONString(startPayload): null;
		
		stepProcessinfInfo.prevProcessingId = prevProcessingInfo!=null? prevProcessingInfo.getProcessingId(): null;
		
		stepProcessinfInfo.stepName = processingStepName;
		stepProcessinfInfo.stepIndex = prevProcessingInfo!=null? prevProcessingInfo.getStepIndex()+1: 0;

		Instant now = Instant.now();
		stepProcessinfInfo.timeProcessingStart = now;
		
		return stepProcessinfInfo;
	}
	
	public static ProcessingInfo createProcessingInfoOnStepEnd(ProcessingInfo startProcessingInfo,
			Header endHeader, Object endPayload) {
		log.debug("Generating end processing info for step {}", startProcessingInfo.getStepName());
		
		ProcessingInfo endProcessinfInfo = createCopy(startProcessingInfo);
		endHeader.setProcessingInfo(endProcessinfInfo); 
		
		endProcessinfInfo.stepFinish(endPayload);
		
		return endProcessinfInfo;
	}

	private void stepFinish(Object endPayload) {
		processingId =  BaseJSONObject.generateUUID();
		
		timeProcessingEnd = Instant.now();
		stepDurationMs = ChronoUnit.MILLIS.between(timeProcessingStart, timeProcessingEnd);
		
		List<UUID> endPayloadEventIds = new ArrayList<>();
		
		if (endPayload instanceof HasEventId) {
			endPayloadEventIds.add(((HasEventId) endPayload).getEventId());
		} else if (endPayload instanceof HasPreviousEventIds) {
			endPayloadEventIds.addAll(((HasPreviousEventIds) endPayload).getPreviousEventIds());
		}
		
		eventIds = endPayloadEventIds.toArray(new UUID[0]);
		
		payloadJsonEnd = JsonUtils.writeObjectToJSONString(endPayload); //this make snapshot of its self. has to be the last call
		
//		calculateDiffToPreviosVersion();
		log.debug("Processing finished for step {}. Took {} ms", getStepName(), getStepDurationMs());
	}

	@Override
	public UUID getId() {		
		return processingId;
	}

	@Override
	public boolean isNew() {
		//Processing info is append only
		return true;
	}

	
//	private void calculateDiffToPreviosVersion() {
//		try {	
//			DiffMatchPatch diff = new DiffMatchPatch();
//			LinkedList<Diff> diffs = diff.diff_main(payloadJson, modifiedPayloadBodyJsonJson);
//			
//			ObjectMapper objectMapper = new ObjectMapper();
//			diffJson = objectMapper.writeValueAsString(diffs.toArray());
//			
//		} catch (JsonProcessingException e) {
//			throw new RuntimeException(e);
//		}
//	}
	
}