package com.obj.nc.domain.headers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.obj.nc.domain.HasEventIds;
import com.obj.nc.domain.HasPreviousIntentIds;
import com.obj.nc.domain.HasPreviousMessageIds;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.repositories.MessageRepository;
import com.obj.nc.repositories.NotificationIntentRepository;
import org.assertj.core.util.Arrays;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.BaseJSONObject;
import com.obj.nc.domain.refIntegrity.Reference;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
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
	
	@NotEmpty
	@Reference(NotificationIntentRepository.class)
	@Builder.Default
	private UUID[] previousIntentIds = new UUID[0];
	
	@NotEmpty
	@Reference(MessageRepository.class)
	@Builder.Default
	private UUID[] previousMessageIds = new UUID[0];
	
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
		
		if (endPayload instanceof GenericEvent) {
			eventIds = new UUID[]{ ((GenericEvent) endPayload).getId() };
		}
		
		if (endPayload instanceof HasEventIds) {
			eventIds = ((HasEventIds) endPayload).getEventIds().toArray(new UUID[0]);
		}
		
		if (endPayload instanceof HasPreviousIntentIds) {
			previousIntentIds = ((HasPreviousIntentIds) endPayload).getPreviousIntentIds().toArray(new UUID[0]);
		}
		
		if (endPayload instanceof HasPreviousMessageIds) {
			previousMessageIds = ((HasPreviousMessageIds) endPayload).getPreviousMessageIds().toArray(new UUID[0]);
		}
		
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