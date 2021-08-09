package com.obj.nc.repositories;

import static java.sql.Timestamp.from;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.refIntegrity.EntityExistanceChecker;

import lombok.extern.log4j.Log4j2;



/**
 * toto nieje sktocne repository v zmysle Springu. Spring data JDB nepodporuje inherintance, co
 * @author ja
 *
 */
@Service
@Log4j2
public class EndpointsRepository implements EntityExistanceChecker<UUID> {
	
    @Autowired
    private JdbcTemplate jdbcTemplate;     
	 
    /**
     * 
     * @param toPersist
     * @return client must use returned list because IDs of endpoints can be different to those provided in input parameter
     */
    public <T extends RecievingEndpoint> List<T> persistEnpointIfNotExists(List<T> toPersist) {
        try  {
        	NewAndExistingEndpoints newAndExisting = findNewAndExisting(toPersist);
	
	        String inserEndpointIfNotExistsSQL = 
	        		"insert into nc_endpoint "
	                + "(id, endpoint_name, endpoint_type, time_created) "
	                + "values "
	                + "(?, ?, ?, ?) ";

	        List<RecievingEndpoint> toInsert = newAndExisting.newEndpoints;
	        List<T> persited = (List<T>)newAndExisting.existingEndpoints;
	        
            jdbcTemplate.batchUpdate(
                    inserEndpointIfNotExistsSQL,
                    new BatchPreparedStatementSetter() {

                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            RecievingEndpoint endpoint = toInsert.get(i);
                            ps.setObject(1, endpoint.getId());
                            ps.setString(2, endpoint.getEndpointId());
                            ps.setString(3, endpoint.getEndpointType());
                            ps.setTimestamp(4, from(Instant.now()));
                            
                            persited.add((T)endpoint);
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


	private <T extends RecievingEndpoint> NewAndExistingEndpoints findNewAndExisting(List<T> toPersist) {
		NewAndExistingEndpoints newAndExisting = new NewAndExistingEndpoints();
		
		List<T> existing = findExistingEndpointsByNameId(toPersist);    	
		for (RecievingEndpoint endPoint: toPersist) {
			if (!endPoint.isNew()) {
				newAndExisting.existingEndpoints.add(endPoint);
				continue;
			}

			Optional<T> existingEndp = existing
					.stream()
					.filter(e -> e.getEndpointId().equals(endPoint.getEndpointId()))
					.findFirst();
			if (existingEndp.isPresent()) {
				newAndExisting.existingEndpoints.add(existingEndp.get());
				continue;
			}
				    		    		
			newAndExisting.newEndpoints.add(endPoint);
		}
		return newAndExisting;
	}
    
    private static class NewAndExistingEndpoints {
    	public List<RecievingEndpoint> existingEndpoints = new ArrayList<RecievingEndpoint>();
    	public List<RecievingEndpoint> newEndpoints = new ArrayList<RecievingEndpoint>();
    }
    

    public Map<String, RecievingEndpoint> persistEnpointIfNotExistsMappedToNameId(List<RecievingEndpoint> toPersist) {
    	List<RecievingEndpoint> persited = persistEnpointIfNotExists(toPersist);
    	
    	return persited.stream().collect(Collectors.toMap(RecievingEndpoint::getEndpointId, Function.identity()));
    }

    
	public <T extends RecievingEndpoint> List<T> findExistingEndpointsByNameId(List<T> ednpoints) {
		List<String> namesList = ednpoints.stream()
                .map(RecievingEndpoint::getEndpointId)
                .collect(Collectors.toList());
		
    	List<T> existing = (List<T>)findByNameIds(namesList.toArray(new String[0]));
    	
    	return existing;
	}
	
	public <T extends RecievingEndpoint> List<T> findExistingEndpointsByIsNewFlag(List<T> ednpoints) {
		List<T> existing = ednpoints.stream()
                .filter(e -> !e.isNew())
                .collect(Collectors.toList());
    	
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
    	if (endpointNames.length==0) {
    		return new ArrayList<RecievingEndpoint>();
    	}
    	
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
	    	  } else if (MailchimpEndpoint.JSON_TYPE_IDENTIFIER.equals(epType)) {
	    		  MailchimpEndpoint mailChimpEndpoint = new MailchimpEndpoint(rs.getString("endpoint_name"));
	              mailChimpEndpoint.setId((UUID)rs.getObject("id"));
	              return mailChimpEndpoint;
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


	@Override
	public boolean existsById(UUID id) {
		return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM nc_endpoint WHERE id = ?)", Boolean.class, id);		
	}



}
