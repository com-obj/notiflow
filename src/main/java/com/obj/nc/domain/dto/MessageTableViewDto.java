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

import lombok.Builder;
import lombok.Data;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class MessageTableViewDto {
    
    private UUID id;
    private Instant timeCreated;
    private UUID[] endpointIds;
    private UUID[] previousEventIds;
    private UUID[] previousIntentIds;
    private UUID[] previousMessageIds;
    
    public static class MessageTableViewDtoRowMapper implements RowMapper<MessageTableViewDto> {
        @Override
        public MessageTableViewDto mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return MessageTableViewDto
                    .builder()
                    .id((UUID) resultSet.getObject("id"))
                    .timeCreated(resultSet.getTimestamp("time_created").toInstant())
                    .endpointIds((UUID[]) resultSet.getArray("endpoint_ids").getArray())
                    .previousEventIds((UUID[]) resultSet.getArray("previous_event_ids").getArray())
                    .previousIntentIds((UUID[]) resultSet.getArray("previous_intent_ids").getArray())
                    .previousMessageIds((UUID[]) resultSet.getArray("previous_message_ids").getArray())
                    .build();
        }
    }
    
}