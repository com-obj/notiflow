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

package com.obj.nc.domain.endpoints;

import com.obj.nc.domain.stats.Stats;
import com.obj.nc.repositories.mappers.ReceivingEndpointRowMapper;
import lombok.Builder;
import lombok.Data;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Data
@Builder
public class ReceivingEndpointWithStats {
    
    private ReceivingEndpoint endpoint;
    private Stats stats;
    
    public static class ReceivingEndpointWithStatsRowMapper implements RowMapper<ReceivingEndpointWithStats> {
    
        private final ReceivingEndpointRowMapper receivingEndpointRowMapper = new ReceivingEndpointRowMapper();
    
        @Override
        public ReceivingEndpointWithStats mapRow(ResultSet resultSet, int i) throws SQLException {
            ReceivingEndpoint endpoint = receivingEndpointRowMapper.mapRow(resultSet, i);
    
            Stats stats = Stats.builder()
                    .eventsCount(resultSet.getLong("events_count"))
//                    .intentsCount(resultSet.getLong("intents_count"))
                    .messagesCount(resultSet.getLong("messages_count"))
                    .endpointsCount(resultSet.getLong("endpoints_count"))
                    .messagesSentCount(resultSet.getLong("messages_sent_count"))
                    .messagesReadCount(resultSet.getLong("messages_read_count"))
                    .messagesFailedCount(resultSet.getLong("messages_failed_count"))
                    .build();
        
            return ReceivingEndpointWithStats
                    .builder()
                    .endpoint(endpoint)
                    .stats(stats)
                    .build();
        }
        
    }
    
}
