package com.obj.nc.functions.sink;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.ProcessingInfo;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.event.Event;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
@AllArgsConstructor
public class ProcessingInfoPersister {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	

	@Bean
	public Consumer<BasePayload> persistPIForEvent() {
		return (event) -> {
			persistPI(event);
		};
	}
	
	@Bean
	public Consumer<BasePayload> persistPIForEventWithRecipients() {
		return (event) -> {
			persistPI(event);
			
			List<RecievingEndpoint> recipients = event.getBody().getRecipients();
			persistEnpointIfNotExists(recipients);
		};
	}
	
	@Bean
	public Consumer<BasePayload> persistPIForMessage() {
		return (message) -> {
			persistPI(message);
		};
	}

	public void persistPI(BasePayload payload) {
		log.debug("Persisting processing info {}",payload);
		
		ProcessingInfo processingInfo = payload.getProcessingInfo();
		
		String inserEventSQL = "INSERT INTO nc_processing_info "
				+ "(payload_id, "
				+ "payload_type, "
				+ "processing_id, "
				+ "prev_processing_id, "
				+ "step_name, "
				+ "step_index, "
				+ "time_processing_start, "
				+ "time_processing_end, "
				+ "event_json, "
				+ "event_json_diff) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?,?, to_json(?::json), to_json(?::json))";
		
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
	
	public void persistEnpointIfNotExists(List<RecievingEndpoint> ednpoints) {
		
		String inserEndpointIfNotExistsSQL = "insert into nc_endpoint "
				+ "(endpoint_name, endpoint_type) "
				+ "values "
				+ "(?, ?) "
				+ "ON CONFLICT ON CONSTRAINT con_pk_endpoint_name DO NOTHING";
		
		jdbcTemplate.batchUpdate(
				inserEndpointIfNotExistsSQL,
				new BatchPreparedStatementSetter() {

					public void setValues(PreparedStatement ps, int i) throws SQLException {
						RecievingEndpoint endpoint = ednpoints.get(i);
						ps.setString(2, endpoint.getName());
						ps.setString(3, endpoint.getEndpointTypeName());
					}

					public int getBatchSize() {
						return ednpoints.size();
					}

				});
		
	}
}
