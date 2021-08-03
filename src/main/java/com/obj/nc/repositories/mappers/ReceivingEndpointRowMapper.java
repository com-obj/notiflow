package com.obj.nc.repositories.mappers;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ReceivingEndpointRowMapper implements RowMapper<RecievingEndpoint> {
    
    @Override
    public RecievingEndpoint mapRow(ResultSet resultSet, int i) throws SQLException {
        RecievingEndpoint endpoint = null;
        
        String epType = resultSet.getString("endpoint_type");
        if (EmailEndpoint.JSON_TYPE_IDENTIFIER.equals(epType)) {
            endpoint = new EmailEndpoint(resultSet.getString("endpoint_name"));
        } else if (SmsEndpoint.JSON_TYPE_IDENTIFIER.equals(epType)) {
            endpoint = new SmsEndpoint(resultSet.getString("endpoint_name"));
        } else {
            throw new RuntimeException("Uknown endpoint type for EndpointsRepository: " + epType);
        }
        
        endpoint.setId((UUID) resultSet.getObject("id"));
        return endpoint;
    }
    
}
