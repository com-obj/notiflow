package com.obj.nc.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.Get;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.utils.DiffMatchPatch;
import com.obj.nc.utils.DiffMatchPatch.Diff;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode.Include;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

	
	public void stepStart(String processingStepName, BasePayload event) {
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
	
	public void stepFinish(BasePayload event) {
		timeStampFinish = Instant.now();
		durationInMs = ChronoUnit.MILLIS.between(timeStampStart, timeStampFinish);
		modifiedEventJson = event.toJSONString();
		
//		calculateDiffToPreviosVersion();
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