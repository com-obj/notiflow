package com.obj.nc.repositories.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;

import com.obj.nc.domain.dto.EndpointDto;

public class EndpointDtoRowMapper implements RowMapper<EndpointDto> {
    
    @Override
    public EndpointDto mapRow(ResultSet resultSet, int i) throws SQLException {
        return EndpointDto.builder()
                .uuid((UUID) resultSet.getObject("id"))
                .name(resultSet.getString("endpoint_name"))
                .type(EndpointDto.EndpointType.valueOf(resultSet.getString("endpoint_type")))
                .sentMessagesCount(resultSet.getLong("sent_messages_count"))
                .build();
    }
    
}
