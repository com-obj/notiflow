package com.obj.nc.repositories.mappers;

import com.obj.nc.domain.event.GenericEventStats;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EventStatsRowMapper implements RowMapper<GenericEventStats> {
    
    @Override
    public GenericEventStats mapRow(ResultSet resultSet, int i) throws SQLException {
        return GenericEventStats.builder()
                .eventsCount(resultSet.getLong("events_count"))
                .intentsCount(resultSet.getLong("intents_count"))
                .messagesCount(resultSet.getLong("messages_count"))
                .endpointsCount(resultSet.getLong("endpoints_count"))
                .messagesSentCount(resultSet.getLong("messages_sent_count"))
                .messagesReadCount(resultSet.getLong("messages_read_count"))
                .messagesFailedCount(resultSet.getLong("messages_failed_count"))
                .build();
    }
    
}
