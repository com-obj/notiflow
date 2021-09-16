/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.repositories.mappers;

import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.event.GenericEventStats;
import com.obj.nc.domain.event.GenericEventWithStats;
import com.obj.nc.repositories.converters.PgObjectToJsonNodeConverter;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class EventStatsRowMapper implements RowMapper<GenericEventWithStats> {
    
    private final PgObjectToJsonNodeConverter converter = new PgObjectToJsonNodeConverter();
    
    @Override
    public GenericEventWithStats mapRow(ResultSet resultSet, int i) throws SQLException {
        GenericEvent event = GenericEvent
                .builder()
                .id((UUID) resultSet.getObject("id"))
                .flowId(resultSet.getString("flow_id"))
                .externalId(resultSet.getString("external_id"))
                .payloadJson(converter.convert((PGobject) resultSet.getObject("payload_json")))
                .timeCreated(resultSet.getTimestamp("time_created").toInstant())
                .timeConsumed(resultSet.getTimestamp("time_consumed").toInstant())
                .payloadType(resultSet.getString("payload_type"))
                .build();
        
        GenericEventStats stats = GenericEventStats.builder()
                .eventsCount(resultSet.getLong("events_count"))
                .intentsCount(resultSet.getLong("intents_count"))
                .messagesCount(resultSet.getLong("messages_count"))
                .endpointsCount(resultSet.getLong("endpoints_count"))
                .messagesSentCount(resultSet.getLong("messages_sent_count"))
                .messagesReadCount(resultSet.getLong("messages_read_count"))
                .messagesFailedCount(resultSet.getLong("messages_failed_count"))
                .build();
        
        return GenericEventWithStats
                .builder()
                .event(event)
                .stats(stats)
                .build();
    }
    
}
