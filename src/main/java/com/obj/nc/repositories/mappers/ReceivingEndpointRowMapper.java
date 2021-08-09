package com.obj.nc.repositories.mappers;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ReceivingEndpointRowMapper implements RowMapper<RecievingEndpoint> {
    
    @Override
    public RecievingEndpoint mapRow(ResultSet rs, int i) throws SQLException {
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
