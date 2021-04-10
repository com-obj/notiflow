package com.obj.nc.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.Get;
import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Include;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
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
	@JsonIgnore
	private String eventJson;
	@Transient
	@JsonIgnore
	private String modifiedEventJson;
	@Transient
	@JsonIgnore
	private String diffJson;
	
	public static List<ProcessingInfo> findProcessingInfo(UUID forEventId, String forStep) {
        List<ProcessingInfo> persistedPIs = Get.getJdbc().query(
        		"select * from nc_processing_info "
        		+ "where "
        			+ "event_ids ? '" +forEventId+ "' "
        			+ "and step_name='" +forStep+ "'",
        		(rs, rowNum) ->
        			new ProcessingInfo(
        					UUID.fromString(rs.getString("processing_id")),
        					UUID.fromString(rs.getString("prev_processing_id")),
        					rs.getString("step_name"),
        					rs.getInt("step_index"),
        					rs.getTimestamp("time_processing_start").toInstant(),
        					rs.getTimestamp("time_processing_end").toInstant(),
        					rs.getLong("step_duration_ms"),
        					rs.getString("event_json"),
        					rs.getString("event_json_diff"),null
        					)
        		);
        return persistedPIs;
	}
	
	public static ProcessingInfo createProcessingInfoOnStepStart(String processingStepName, Header startHeader, Object startPayload) {
		log.info("Generating start processing info for step {}", processingStepName);
		
		ProcessingInfo stepProcessinfInfo = new ProcessingInfo();
		stepProcessinfInfo.eventJson = JsonUtils.writeObjectToJSONString(startPayload);
		
		ProcessingInfo prevProcessingInfo = startHeader.getProcessingInfo();
		stepProcessinfInfo.prevProcessingId = prevProcessingInfo!=null? prevProcessingInfo.getProcessingId(): null;
		
		stepProcessinfInfo.stepName = processingStepName;
		stepProcessinfInfo.stepIndex = prevProcessingInfo!=null? prevProcessingInfo.getStepIndex()+1: 0;

		Instant now = Instant.now();
		stepProcessinfInfo.timeStampStart = now;
		
		return stepProcessinfInfo;
	}
	
	public static ProcessingInfo createProcessingInfoOnStepEnd(ProcessingInfo startProcessingInfo,
			Header endHeader, Object endPayload) {
		log.info("Generating end processing info for step {}", startProcessingInfo.getStepName());
		
		ProcessingInfo endProcessinfInfo = new ProcessingInfo();
		endProcessinfInfo.eventJson = startProcessingInfo.eventJson;
		endProcessinfInfo.prevProcessingId = startProcessingInfo.prevProcessingId;
		endProcessinfInfo.stepName = startProcessingInfo.stepName;
		endProcessinfInfo.stepIndex = startProcessingInfo.stepIndex;
		endProcessinfInfo.timeStampStart = startProcessingInfo.timeStampStart;

		endProcessinfInfo.stepFinish(endHeader, endPayload);
		
		return endProcessinfInfo;
	}

	private void stepFinish(Header endHeader, Object endPayload) {
		processingId =  BaseJSONObject.generateUUID();
		
		timeStampFinish = Instant.now();
		durationInMs = ChronoUnit.MILLIS.between(timeStampStart, timeStampFinish);
		modifiedEventJson = JsonUtils.writeObjectToJSONString(endPayload);
		
//		calculateDiffToPreviosVersion();
		log.info("Processing finished for step {}. Took {} ms", getStepName(), getDurationInMs());
	}

	
//	private void calculateDiffToPreviosVersion() {
//		try {	
//			DiffMatchPatch diff = new DiffMatchPatch();
//			LinkedList<Diff> diffs = diff.diff_main(eventJson, modifiedEventJson);
//			
//			ObjectMapper objectMapper = new ObjectMapper();
//			diffJson = objectMapper.writeValueAsString(diffs.toArray());
//			
//		} catch (JsonProcessingException e) {
//			throw new RuntimeException(e);
//		}
//	}
	
}