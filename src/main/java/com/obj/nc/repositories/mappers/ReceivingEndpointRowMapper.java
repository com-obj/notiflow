/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.repositories.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.obj.nc.domain.endpoints.push.DirectPushEndpoint;
import com.obj.nc.domain.endpoints.push.TopicPushEndpoint;
import org.springframework.jdbc.core.RowMapper;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;

public class ReceivingEndpointRowMapper implements RowMapper<ReceivingEndpoint> {
    
    @Override
    public ReceivingEndpoint mapRow(ResultSet rs, int i) throws SQLException {
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
        } else if (DirectPushEndpoint.JSON_TYPE_IDENTIFIER.equals(epType)) {
            DirectPushEndpoint directPushEndpoint = DirectPushEndpoint
                    .builder()
                    .token(rs.getString("endpoint_name"))
                    .build();
            directPushEndpoint.setId((UUID)rs.getObject("id"));
            return directPushEndpoint;
        } else if (TopicPushEndpoint.JSON_TYPE_IDENTIFIER.equals(epType)) {
            TopicPushEndpoint topicPushEndpoint = TopicPushEndpoint
                    .builder()
                    .topic(rs.getString("endpoint_name"))
                    .build();
            topicPushEndpoint.setId((UUID)rs.getObject("id"));
            return topicPushEndpoint;
        } else {
            throw new RuntimeException("Uknown endpoint type for EndpointsRepository: "+ epType);
        }
    }
    
}
