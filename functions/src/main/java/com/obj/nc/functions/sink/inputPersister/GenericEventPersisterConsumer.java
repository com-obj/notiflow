package com.obj.nc.functions.sink.inputPersister;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sink.SinkConsumerAdapter;
import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
public class GenericEventPersisterConsumer extends SinkConsumerAdapter<GenericEvent> {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
	protected Optional<PayloadValidationException> checkPreCondition(GenericEvent payload) {
		if (payload == null) {
			return Optional.of(new PayloadValidationException("Could not persist GenericEvent because its null. Payload: " + payload));
		}
		if (payload.getFlowId()==null) {
			return Optional.of(new PayloadValidationException("Could not persist GenericEvent because flowId is null. Payload: " + payload));
		}

		return Optional.empty();
	}
	
    protected void execute(GenericEvent payload) {
        log.debug("Persisting generic event {}",payload);

        String inserEventSQL = "INSERT INTO nc_input "
                + "(payload_id, "
                + "flow_id, "
                + "external_id, "
                + "payload_json) "
                + "VALUES (?, ?, ?, to_json(?::json))";

        jdbcTemplate.update(
        		inserEventSQL,
        		payload.getPayloadId(),
        		payload.getFlowId(),
                payload.getExternalId(),
                JsonUtils.writeObjectToJSONString(payload.getState()));
    }
    
    public GenericEvent findByPayloadId(UUID pyalodId) {
    	GenericEvent event = 
    			jdbcTemplate.queryForObject(
    					"select payload_id, flow_id, external_id, payload_json "
    					+ "from nc_input "
    					+ "where payload_id = ?", 
    			new Object[] {pyalodId}, 
    			new GenericEventRowMapper());
    	
    	return event;
    }

    public class GenericEventRowMapper implements RowMapper<GenericEvent> {
        @Override
        public GenericEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        	GenericEvent event = new GenericEvent();

        	event.setPayloadId(UUID.fromString(rs.getString("payload_id")));
        	event.setFlowId(rs.getString("flow_id"));
        	event.setExternalId(rs.getString("external_id"));
            event.setState(JsonUtils.readObjectFromJSONString(rs.getString("payload_json")));

            return event;
        }
    }
}
