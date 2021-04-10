package com.obj.nc.repositories;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.headers.ProcessingInfo;

import lombok.extern.log4j.Log4j2;



/**
 * toto nieje sktocne repository v zmysle Springu. Raz bude ale zatial takto
 * @author ja
 *
 */
@Service
@Log4j2
public class HeaderRepository {
	
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private EndpointsRepository endpointsRepository;
	
    public void persistPI(Header header) {
        log.debug("Persisting processing info {}",header);

        ProcessingInfo processingInfo = header.getProcessingInfo();

        String inserEventSQL = "INSERT INTO nc_processing_info "
                + "(event_ids, "
                + "payload_id, "
                + "payload_type, "
                + "processing_id, "
                + "prev_processing_id, "
                + "step_name, "
                + "step_index, "
                + "time_processing_start, "
                + "time_processing_end, "
                + "step_duration_ms, "
                + "event_json, "
                + "event_json_diff) "
                + "VALUES (to_json(?::json), ?, ?, ?, ?, ?, ?, ?, ?, ?, to_json(?::json), to_json(?::json))";

        long stepStartMs = processingInfo.getTimeStampStart().toEpochMilli();
        long stepEndMs = processingInfo.getTimeStampFinish().toEpochMilli();

        long stepDurationMs = stepEndMs - stepStartMs;

        jdbcTemplate.update(inserEventSQL,
        		header.eventIdsAsJSONString(),
        		header.getId(),
//TODO                payload.getPayloadTypeName(),
        		null,  
                processingInfo.getProcessingId(),
                processingInfo.getPrevProcessingId(),
                processingInfo.getStepName(),
                processingInfo.getStepIndex(),
                new Timestamp(stepStartMs),
                new Timestamp(stepEndMs),
                stepDurationMs,
                processingInfo.getEventJson(),
                processingInfo.getDiffJson());
    }
    
    public void persistProcessingInfoWithRecipients(BasePayload payload) {
    	persistPI(payload.getHeader());
    	
        List<RecievingEndpoint> recipients = payload.getBody().getRecievingEndpoints();
        UUID processingId = payload.getProcessingInfo().getProcessingId();

        endpointsRepository.persistEnpointIfNotExists(recipients);
        endpointsRepository.persistEnpoint2Processing(processingId, recipients);
    	
    }


}
