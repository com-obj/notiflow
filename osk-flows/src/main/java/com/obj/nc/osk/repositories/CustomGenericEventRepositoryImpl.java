package com.obj.nc.osk.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public class CustomGenericEventRepositoryImpl implements CustomGenericEventRepository {
	
	@Autowired private JdbcTemplate jdbcTemplate; 

	@Override
	public Optional<GenericEvent> findStartEventByEventId(Long eventId) {
		//TODO: ADD HASH INDEX TO DB on payload_json->'id'
		String query = 
				"select id, flow_id, external_id, payload_json, time_created, time_consumed from nc_event "
        		+ "where (payload_json->'id')::int4 = " +eventId
        		+ " AND payload_json->'@type' = '\"OUTAGE_START\"'";

		List<GenericEvent> events = jdbcTemplate.query(query
        		,
        		(rs, rowNum) -> {
	        		 return GenericEvent.builder()
	        				.id(UUID.fromString(rs.getString("id")))
	        				.flowId(rs.getString("flow_id"))
	        				.externalId(rs.getString("external_id"))
	        				.timeCreated(rs.getTimestamp("time_created").toInstant())
	        				.timeConsumed(rs.getTimestamp("time_consumed")== null?null:rs.getTimestamp("time_consumed").toInstant())
	        				.payloadJson(JsonUtils.readJsonNodeFromJSONString(rs.getString("payload_json")))
	        				.build();
       		});
		 
		if (events.size() == 0) {
			return Optional.empty();
		}
		if (events.size() == 1) {
			return Optional.of(events.iterator().next());
		}
		
		throw new RuntimeException("Found more than one OUTAGE_START event with ID: " + eventId);
	}

}
