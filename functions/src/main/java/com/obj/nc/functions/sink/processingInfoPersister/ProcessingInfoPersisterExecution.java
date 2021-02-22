package com.obj.nc.functions.sink.processingInfoPersister;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.ProcessingInfo;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Log4j2
public class ProcessingInfoPersisterExecution implements Consumer<BasePayload> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void accept(BasePayload payload) {
        persistPI(payload);
    }

    public void persistPI(BasePayload payload) {
        log.debug("Persisting processing info {}",payload);

        ProcessingInfo processingInfo = payload.getProcessingInfo();

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
                payload.getHeader().eventIdsAsJSONString(),
                payload.getHeader().getId(),
                payload.getPayloadTypeName(),
                processingInfo.getProcessingId(),
                processingInfo.getPrevProcessingId(),
                processingInfo.getStepName(),
                processingInfo.getStepIndex(),
                new Timestamp(stepStartMs),
                new Timestamp(stepEndMs),
                stepDurationMs,
                payload.toJSONString(),
                processingInfo.getDiffJson());
    }

    public void persistEnpointIfNotExists(List<RecievingEndpoint> ednpoints) {

        String inserEndpointIfNotExistsSQL = "insert into nc_endpoint "
                + "(endpoint_name, endpoint_type) "
                + "values "
                + "(?, ?) "
                + "ON CONFLICT ON CONSTRAINT con_pk_endpoint_name DO NOTHING";
        try  {
            jdbcTemplate.batchUpdate(
                    inserEndpointIfNotExistsSQL,
                    new BatchPreparedStatementSetter() {

                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            RecievingEndpoint endpoint = ednpoints.get(i);
                            ps.setString(1, endpoint.getEndpointId());
                            ps.setString(2, endpoint.getEndpointTypeName());
                        }

                        public int getBatchSize() {
                            return ednpoints.size();
                        }

                    });
        } catch (RuntimeException e) {
            log.error(e);
            throw e;
        }
    }

    public void persistEnpoint2Processing(UUID processingId, List<RecievingEndpoint> ednpoints) {

        String inserEndpoint2ProcessingRelSQL = "insert into nc_endpoint_processing "
                + "(endpoint_id, processing_id) "
                + "values "
                + "(?, ?) ";

        try  {
            jdbcTemplate.batchUpdate(
                    inserEndpoint2ProcessingRelSQL,
                    new BatchPreparedStatementSetter() {

                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            RecievingEndpoint endpoint = ednpoints.get(i);
                            ps.setString(1, endpoint.getEndpointId());
                            ps.setObject(2, processingId);
                        }

                        public int getBatchSize() {
                            return ednpoints.size();
                        }

                    });
        } catch (RuntimeException e) {
            log.error(e);
            throw e;
        }

    }

}