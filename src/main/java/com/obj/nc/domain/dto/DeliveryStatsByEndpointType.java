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

package com.obj.nc.domain.dto;


import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeliveryStatsByEndpointType {
    
    private String endpointType;
    private long messagesCount;
    private long endpointsCount;
    private long messagesSentCount;
    private long messagesReadCount;
    private long messagesFailedCount;
    private long messagesDeliveredCount;
    private long messagesDeliveryPendingCount;
    private long messagesDeliveryUnknownCount;
    private long messagesDeliveryFailedCount;
    private long messagesProcessingCount;
    private long messagesDiscardedCount;

    public static class DeliveryStatsByEndpointTypeRowMapper implements RowMapper<DeliveryStatsByEndpointType> {
        
        @Override
        public DeliveryStatsByEndpointType mapRow(ResultSet resultSet, int i) throws SQLException {
            
            DeliveryStatsByEndpointType stats = DeliveryStatsByEndpointType.builder()
                    .endpointType(resultSet.getString("endpoint_type"))
                    .messagesCount(resultSet.getLong("messages_count"))
                    .endpointsCount(resultSet.getLong("endpoints_count"))
                    .messagesSentCount(resultSet.getLong("messages_sent_count"))
                    .messagesReadCount(resultSet.getLong("messages_read_count"))
                    .messagesFailedCount(resultSet.getLong("messages_failed_count"))
                    .messagesDeliveredCount(resultSet.getLong("messages_delivered_count"))
                    .messagesDeliveryPendingCount(resultSet.getLong("messages_delivery_pending_count"))
                    .messagesDeliveryUnknownCount(resultSet.getLong("messages_delivery_unknown_count"))
                    .messagesDeliveryFailedCount(resultSet.getLong("messages_delivery_failed_count"))
                    .messagesProcessingCount(resultSet.getLong("messages_processing_count"))
                    .messagesDiscardedCount(resultSet.getLong("messages_discarded_count"))
                    .build();
            
            return stats;
        }
        
    }

    
}
