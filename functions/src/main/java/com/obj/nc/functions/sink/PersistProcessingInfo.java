package com.obj.nc.functions.sink;

import java.sql.Timestamp;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.ProcessingInfo;
import com.obj.nc.domain.event.Event;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
@AllArgsConstructor
public class PersistProcessingInfo {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	

	@Bean
	public Consumer<BasePayload> persistPI() {
		return (event) -> {
			persistPI(event);
		};
	}
	
	@Bean
	public Consumer<BasePayload> persistPI2() {
		return (event) -> {
			persistPI(event);
		};
	}
	
	@Bean
	public Consumer<BasePayload> persistPI3() {
		return (event) -> {
			persistPI(event);
		};
	}

	public void persistPI(BasePayload payload) {
		log.debug("Persisting processing info {}",payload);
		
		ProcessingInfo processingInfo = payload.getProcessingInfo();
		
		String inserEventSQL = "INSERT INTO nc_processing_info (payload_id, payload_type, processing_id, prev_processing_id, step_name, step_index, time_processing_start, time_processing_end, event_json, event_json_diff) VALUES (?, ?, ?, ?, ?, ?, ?,?, to_json(?::json), to_json(?::json))";
		
		Timestamp processingTimeStampStart = new Timestamp(processingInfo.getTimeStampStart().toEpochMilli());
		Timestamp processingTimeStampFinish = new Timestamp(processingInfo.getTimeStampFinish().toEpochMilli());
		
		jdbcTemplate.update(inserEventSQL,
				payload.getHeader().getId(),
				payload.getPayloadTypeName(),
				processingInfo.getProcessingId(),
				processingInfo.getPrevProcessingId(),
				processingInfo.getStepName(),
				processingInfo.getStepIndex(),
				processingTimeStampStart,
				processingTimeStampFinish,
				payload.toJSONString(),
				processingInfo.getDiffJson());
	}
}
