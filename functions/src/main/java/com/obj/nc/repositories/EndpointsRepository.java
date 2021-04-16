package com.obj.nc.repositories;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.obj.nc.domain.endpoints.RecievingEndpoint;

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
                + "(endpoint_id, endpoint_type) "
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
                            ps.setString(2, endpoint.getEndpointType());
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
    
    public void persistEnpointIfNotExists(RecievingEndpoint ednpoint) {
    	List<RecievingEndpoint> ednpoints = Arrays.asList(ednpoint);
    	
    	persistEnpointIfNotExists(ednpoints);
    }



}
