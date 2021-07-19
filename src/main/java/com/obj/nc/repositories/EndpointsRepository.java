package com.obj.nc.repositories;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.obj.nc.repositories.converters.PgObjectToUUIDArrayConverter;
import com.obj.nc.repositories.converters.UUIDArrayToPgObjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
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
	 
    public void persistEnpointIfNotExists(List<RecievingEndpoint> ednpoints) {

        String inserEndpointIfNotExistsSQL = 
        		"insert into nc_endpoint "
                + "(id, endpoint_name, endpoint_type) "
                + "values "
                + "(?, ?, ?) "
                + "ON CONFLICT ON CONSTRAINT nc_endpoint_endpoint_id_unique_key DO NOTHING";
        try  {
            jdbcTemplate.batchUpdate(
                    inserEndpointIfNotExistsSQL,
                    new BatchPreparedStatementSetter() {

                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            RecievingEndpoint endpoint = ednpoints.get(i);
                            ps.setObject(1, endpoint.getId());
                            ps.setString(2, endpoint.getEndpointId());
                            ps.setString(3, endpoint.getEndpointType());
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
    
    public List<RecievingEndpoint> findByIds(UUID ... endpointIds) {
    	String query = 
        		"select id, endpoint_name, endpoint_type "
                + "from nc_endpoint "
                + "where id in (%s)";  	
    	String inSql = String.join(",", Collections.nCopies(endpointIds.length, "?"));
    	query = String.format(query, inSql);
    	
        return jdbcTemplate.query(query, EndpointsRepository::mapRow, (Object[])endpointIds);
    }
    
    public List<RecievingEndpoint> findAll() {
        String query = "select id, endpoint_name, endpoint_type from nc_endpoint ";
        return jdbcTemplate.query(query, EndpointsRepository::mapRow);
    }
    
    private static RecievingEndpoint mapRow(ResultSet rs, int rowNum) throws SQLException {
        String epType = rs.getString("endpoint_type");
        
        if (EmailEndpoint.JSON_TYPE_IDENTIFIER.equals(epType)) {
            EmailEndpoint emailEndpoint = new EmailEndpoint(rs.getString("endpoint_name"));
            emailEndpoint.setId((UUID) rs.getObject("id"));
            return emailEndpoint;
        } else if (SmsEndpoint.JSON_TYPE_IDENTIFIER.equals(epType)) {
            SmsEndpoint smsEndpoint = new SmsEndpoint(rs.getString("endpoint_name"));
            smsEndpoint.setId((UUID) rs.getObject("id"));
            return smsEndpoint;
        } else {
            throw new RuntimeException("Uknown endpoint type for EndpointsRepository: " + epType);
        }
    }
    
    public void persistEnpointIfNotExists(RecievingEndpoint ednpoint) {
    	List<RecievingEndpoint> ednpoints = Arrays.asList(ednpoint);
    	
    	persistEnpointIfNotExists(ednpoints);
    }
    
    public void persistEnpointIfNotExists(RecievingEndpoint ... ednpoints) {
    	persistEnpointIfNotExists(Arrays.asList(ednpoints));
    }



}
