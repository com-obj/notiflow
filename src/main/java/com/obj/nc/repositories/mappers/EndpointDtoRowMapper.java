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
