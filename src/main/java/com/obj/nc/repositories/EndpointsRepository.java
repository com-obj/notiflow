package com.obj.nc.repositories;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.obj.nc.repositories.converters.PgObjectToUUIDArrayConverter;
import com.obj.nc.repositories.converters.UUIDArrayToPgObjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;

import lombok.extern.log4j.Log4j2;



/**
 * toto nieje sktocne repository v zmysle Springu. Spring data JDB nepodporuje inherintance, co
 * @author ja
 *
 */
@Service
@Log4j2
public class EndpointsRepository {
	
    @Autowired
    private JdbcTemplate jdbcTemplate;     
	 
    /**
     * 
     * @param toPersist
     * @return client must user returned list because IDs of endoints can be different to those provided in input parameter
     */
    public List<RecievingEndpoint> persistEnpointIfNotExists(List<RecievingEndpoint> toPersist) {
        try  {
        	List<RecievingEndpoint> persited = findExistingEndpointsByNameId(toPersist);
    	
	    	List<RecievingEndpoint> toInsert = new ArrayList<RecievingEndpoint>();
	    	for (RecievingEndpoint endPoint: toPersist) {
	    		if (persited.stream().anyMatch(e -> e.getEndpointId().equals(endPoint.getEndpointId()))) {
	    			continue;
	    		}
	    		
	    		toInsert.add(endPoint);	    		
	    	}
	
	        String inserEndpointIfNotExistsSQL = 
	        		"insert into nc_endpoint "
	                + "(id, endpoint_name, endpoint_type) "
	                + "values "
	                + "(?, ?, ?) ";

            jdbcTemplate.batchUpdate(
                    inserEndpointIfNotExistsSQL,
                    new BatchPreparedStatementSetter() {

                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            RecievingEndpoint endpoint = toInsert.get(i);
                            ps.setObject(1, endpoint.getId());
                            ps.setString(2, endpoint.getEndpointId());
                            ps.setString(3, endpoint.getEndpointType());
                            
                            persited.add(endpoint);
                        }

                        public int getBatchSize() {
                            return toInsert.size();
                        }

                    });
            
            return persited;
        } catch (RuntimeException e) {
            log.error(e);
            throw e;
        }
    }

    public Map<String, RecievingEndpoint> persistEnpointIfNotExistsMappedToNameId(List<RecievingEndpoint> toPersist) {
    	List<RecievingEndpoint> persited = persistEnpointIfNotExists(toPersist);
    	
    	return persited.stream().collect(Collectors.toMap(RecievingEndpoint::getEndpointId, Function.identity()));
    }

    
	public List<RecievingEndpoint> findExistingEndpointsByNameId(List<RecievingEndpoint> ednpoints) {
		List<String> namesList = ednpoints.stream()
                .map(RecievingEndpoint::getEndpointId)
                .collect(Collectors.toList());
		
    	List<RecievingEndpoint> existing = findByNameIds(namesList.toArray(new String[0]));
    	
    	return existing;
	}
    
    public List<RecievingEndpoint> findByIds(UUID ... endpointIds) {
    	String query = 
        		"select id, endpoint_name, endpoint_type "
                + "from nc_endpoint "
                + "where id in (%s)";  	
    	String inSql = String.join(",", Collections.nCopies(endpointIds.length, "?"));
    	query = String.format(query, inSql);
    	
    	List<RecievingEndpoint> endpoints = jdbcTemplate.query(
    			query,
    			new EndpointRowMapper(), 
    			(Object[])endpointIds
        );
        
        return endpoints;
    }
    
    public List<RecievingEndpoint> findByNameIds(String ... endpointNames) {
    	String query = 
        		"select id, endpoint_name, endpoint_type "
                + "from nc_endpoint "
                + "where endpoint_name in (%s)";  	
    	String inSql = String.join(",", Collections.nCopies(endpointNames.length, "?"));
    	query = String.format(query, inSql);
    	
    	List<RecievingEndpoint> endpoints = jdbcTemplate.query(
    			query,
    			new EndpointRowMapper(), 
    			(Object[])endpointNames
    	);
        
        return endpoints;
    }
    
    public static class EndpointRowMapper implements RowMapper<RecievingEndpoint> {

		@Override
		public RecievingEndpoint mapRow(ResultSet rs, int rowNum) throws SQLException {
			 String epType = rs.getString("endpoint_type");
	    	  
	    	  if (EmailEndpoint.JSON_TYPE_IDENTIFIER.equals(epType)) {
                 EmailEndpoint emailEndpoint = new EmailEndpoint(rs.getString("endpoint_name"));
                 emailEndpoint.setId((UUID)rs.getObject("id"));
                 return emailEndpoint;
	    	  } else if (SmsEndpoint.JSON_TYPE_IDENTIFIER.equals(epType)) {
                 SmsEndpoint smsEndpoint = new SmsEndpoint(rs.getString("endpoint_name"));
                 smsEndpoint.setId((UUID)rs.getObject("id"));
                 return smsEndpoint;
	    	  } else {
	    		  throw new RuntimeException("Uknown endpoint type for EndpointsRepository: "+ epType);
	    	  }
		}
    	
    }
    
    public <T extends RecievingEndpoint> T persistEnpointIfNotExists(T ednpoint) {
    	List<RecievingEndpoint> ednpoints = Arrays.asList(ednpoint);
    	
    	RecievingEndpoint persisted = persistEnpointIfNotExists(ednpoints).iterator().next();
    	return (T)persisted;
    }
    
    public List<RecievingEndpoint> persistEnpointIfNotExists(RecievingEndpoint ... ednpoints) {
    	return persistEnpointIfNotExists(Arrays.asList(ednpoints));
    }
    
    public Map<String, RecievingEndpoint> persistEnpointIfNotExistsMappedToNameId(RecievingEndpoint ... ednpoints) {
    	return persistEnpointIfNotExistsMappedToNameId(Arrays.asList(ednpoints));
    }



}
