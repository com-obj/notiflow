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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;

import com.obj.nc.domain.dto.EndpointTableViewDto;

public class EndpointDtoRowMapper implements RowMapper<EndpointTableViewDto> {
    
    @Override
    public EndpointTableViewDto mapRow(ResultSet resultSet, int i) throws SQLException {
        return EndpointTableViewDto
                .builder()
                .uuid((UUID) resultSet.getObject("id"))
                .name(resultSet.getString("endpoint_name"))
                .type(EndpointTableViewDto.EndpointType.valueOf(resultSet.getString("endpoint_type")))
                .sentMessagesCount(resultSet.getLong("sent_messages_count"))
                .build();
    }
    
}
