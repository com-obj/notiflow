package com.obj.nc.repositories.mappers;

import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.SlackEndpoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ReceivingEndpointRowMapperTest {
    @Test
    void testSlackEndpointMapping() throws SQLException {
        ResultSet resultSet =  Mockito.mock(ResultSet.class);

        UUID uuid = UUID.randomUUID();
        String endpointName = "channel-name";

        Mockito.when(resultSet.getString("endpoint_type")).thenReturn(SlackEndpoint.JSON_TYPE_IDENTIFIER);
        Mockito.when(resultSet.getString("endpoint_name")).thenReturn(endpointName);
        Mockito.when(resultSet.getObject("id")).thenReturn(uuid);

        SlackEndpoint endpoint = (SlackEndpoint) new ReceivingEndpointRowMapper().mapRow(resultSet, 0);
        Assertions.assertNotNull(endpoint);
        Assertions.assertEquals(uuid, endpoint.getId());
        Assertions.assertEquals(endpointName, endpoint.getEndpointId());
    }
}
