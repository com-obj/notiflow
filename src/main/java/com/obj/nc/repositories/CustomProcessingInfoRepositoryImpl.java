package com.obj.nc.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.obj.nc.domain.headers.ProcessingInfo;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public class CustomProcessingInfoRepositoryImpl implements CustomProcessingInfoRepository {
	
	@Autowired private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate; 
	
	public List<ProcessingInfo> findByAnyEventIdAndStepName(UUID forEventId, String forStep) {
        List<ProcessingInfo> persistedPIs = jdbcTemplate.query(
        		"select * from nc_processing_info "
        		+ "where "
        			+ "'" +forEventId+ "' = ANY (event_ids) " 
        			+ "and step_name='" +forStep+ "'",
        		(rs, rowNum) -> {
	        		 return ProcessingInfo.builder()
	        				.processingId(UUID.fromString(rs.getString("processing_id")))
	        				.prevProcessingId(
	        						rs.getString("prev_processing_id") == null?
	        							null
	        						:
	        							UUID.fromString(rs.getString("prev_processing_id")))
	        				.stepName(rs.getString("step_name"))
	        				.stepIndex(rs.getInt("step_index"))
	        				.timeProcessingStart(rs.getTimestamp("time_processing_start").toInstant())
	        				.timeProcessingEnd(rs.getTimestamp("time_processing_end").toInstant())
	        				.stepDurationMs(rs.getLong("step_duration_ms"))
	        				.payloadJsonStart(rs.getString("payload_json_start"))
	        				.payloadJsonEnd(rs.getString("payload_json_end"))
	        				.eventIds((UUID[])rs.getArray("event_ids").getArray())
	        				.previousIntentIds((UUID[])rs.getArray("intent_ids").getArray())
	        				.previousMessageIds((UUID[])rs.getArray("message_ids").getArray())
	        				.build();
        		}
        	);
        
        return persistedPIs;
	}

}
