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

package com.obj.nc.domain.dto.fe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.Get;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

import static com.obj.nc.utils.JsonUtils.getObjectMapperWithSortedProperties;

@Data
@Builder
public class MessageDto {
    private final String id;
    private final String flowId;
    private final Instant timeCreated;
    private final String latestStatus;
    private final EndpointDto[] endpoints;
    private final EventDto[] events;

    public static class MessageTableViewDtoRowMapper implements RowMapper<MessageDto> {
        @Override
        public MessageDto mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            EndpointDto[] endpoints = {};
            EventDto[] events = {};

            String ep = resultSet.getString("endpoints");
            String ev = resultSet.getString("events");


            try {
                final EndpointDto[] endpointsRow = getObjectMapperWithSortedProperties().readValue(
                        ep, new TypeReference<EndpointDto[]>() {}
                );

                endpoints = endpointsRow != null ? endpointsRow : endpoints;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            try {
                final EventDto[] eventsRow = getObjectMapperWithSortedProperties().readValue(
                        ev, new TypeReference<EventDto[]>() {}
                );

                events = eventsRow != null ? eventsRow : events;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            return MessageDto.builder()
                    .id(resultSet.getString("id"))
                    .flowId(resultSet.getString("flow_id"))
                    .timeCreated(resultSet.getTimestamp("time_created").toInstant())
                    .latestStatus(resultSet.getString("latest_status"))
                    .endpoints(endpoints)
                    .events(events)
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    static class EndpointDto {
        private String id;
        private String name;
        private String type;
    }

    @Data
    @NoArgsConstructor
    static class EventDto {
        private String id;
        private String name;
        private String description;
    }
}