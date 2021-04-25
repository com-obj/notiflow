package com.obj.nc.repositories;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.postgresql.jdbc.PgArray;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.domain.Body;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.repositories.converters.JsonNodeToPgObjectConverter;
import com.obj.nc.repositories.converters.PgObjectToJsonNodeConverter;
import com.obj.nc.repositories.converters.PgObjectToUUIDArrayConverter;
import com.obj.nc.repositories.converters.UUIDArrayToPgObjectConverter;
import com.obj.nc.utils.JsonUtils;

import lombok.extern.log4j.Log4j2;



/**
 * toto nieje sktocne repository v zmysle Springu. prerbit po refactoringu NotificationIntent
 * @author ja
 *
 */
@Service
@Log4j2
public class NotificationIntentRepository {
	
    @Autowired
    private JdbcTemplate jdbcTemplate;  
    
    @Autowired
    private NamedParameterJdbcTemplate jdbcNamedTemplate;  
    
    private UUIDArrayToPgObjectConverter uuidsToPGObject  = new UUIDArrayToPgObjectConverter();
    private JsonNodeToPgObjectConverter jsonToPGObject  = new JsonNodeToPgObjectConverter();
	 
    public NotificationIntent save(NotificationIntent intent) {
        try  {
	    	if (intent.getId() == null) {
	    		intent.setId(UUID.randomUUID());
	    	}
	    	if (intent.getTimeCreated() == null) {
	    		intent.setTimeCreated(Instant.now());
	    	}
	
	        String insert = 
	        		"insert into nc_intent "
	                + "(id, flow_id, event_ids, payload_json, time_created) "
	                + "values "
	                + "(:id, :flow_id, :event_ids, :payload_json, :time_created) ";
	        
	        Map<String, Object> params = new HashMap<>();
	        params.put("id",intent.getId()); 
	        params.put("flow_id",intent.getHeader().getFlowId()); 
	        params.put("event_ids",uuidsToPGObject.convert(intent.getHeader().getEventIdsAsArray())); 
	        params.put("payload_json",jsonToPGObject.convert(intent.getBody().toJSONNode())); 
	        params.put("time_created",Timestamp.from(intent.getTimeCreated())); 
        
        	jdbcNamedTemplate.update(
            		insert,
            		params);
            
            return intent;
        } catch (RuntimeException e) {
            log.error(e);
            throw e;
        }
    }
    
    private PgObjectToJsonNodeConverter PGObjectToJsonNode  = new PgObjectToJsonNodeConverter();
    
    public Optional<NotificationIntent> findById(UUID intentId) {
    	List<NotificationIntent> intents = findByIds(intentId);
    	
    	if (intents.size() == 0) {
    		return Optional.empty();
    	}
    	
    	if (intents.size() == 1) {
    		return Optional.of(intents.iterator().next());
    	}
    	
    	throw new RuntimeException();
    }
    
    public List<NotificationIntent> findByIds(UUID ... intentIds) {
    	String query = 
        		"select id, flow_id, event_ids, payload_json, time_created "
                + "from nc_intent "
                + "where id in (%s)";  	
    	String inSql = String.join(",", Collections.nCopies(intentIds.length, "?"));
    	query = String.format(query, inSql);
    	
    	List<NotificationIntent> intents = jdbcTemplate.query(
    			query,
        		(rs, rowNum) -> {
        			NotificationIntent intent = new NotificationIntent();
        			intent.setId(UUID.fromString(rs.getString("id")));
        			intent.setTimeCreated(rs.getTimestamp("time_created").toInstant());
        			intent.getHeader().setFlowId(rs.getString("flow_id"));
        			
        			PgArray array = (PgArray)rs.getArray("event_ids");
        			UUID[] eventIds = (UUID[])array.getArray();
        			intent.getHeader().setEventIdsAsArray(eventIds);
        			
        			JsonNode json = PGObjectToJsonNode.convert((PGobject)rs.getObject("payload_json"));
        			Body body = JsonUtils.readObjectFromJSON(json, Body.class);
        			intent.setBody(body);
        			
        			return intent;
        	   } 
        	, (Object[])intentIds
        );
        
        return intents;
    }


}
