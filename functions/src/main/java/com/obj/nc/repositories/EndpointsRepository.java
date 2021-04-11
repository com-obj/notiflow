package com.obj.nc.repositories;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.obj.nc.domain.endpoints.RecievingEndpoint;

import lombok.extern.log4j.Log4j2;



/**
 * toto nieje sktocne repository v zmysle Springu. Raz bude ale zatial takto
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

//        String inserEndpoint2ProcessingRelSQL = 
//        		"insert into nc_event_2_endpoint_delivery "
//                + "(endpoint_id, processing_id) "
//                + "values "
//                + "(?, ?) ";
//
//        try  {
//            jdbcTemplate.batchUpdate(
//                    inserEndpoint2ProcessingRelSQL,
//                    new BatchPreparedStatementSetter() {
//
//                        public void setValues(PreparedStatement ps, int i) throws SQLException {
//                            RecievingEndpoint endpoint = ednpoints.get(i);
//                            ps.setString(1, endpoint.getEndpointId());
//                            ps.setObject(2, processingId);
//                        }
//
//                        public int getBatchSize() {
//                            return ednpoints.size();
//                        }
//
//                    });
//        } catch (RuntimeException e) {
//            log.error(e);
//            throw e;
//        }

    }


}
