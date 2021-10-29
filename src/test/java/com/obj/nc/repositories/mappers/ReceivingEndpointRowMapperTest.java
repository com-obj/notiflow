package com.obj.nc.repositories.mappers;

import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.SlackEndpoint;
import com.obj.nc.domain.endpoints.TeamsEndpoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ReceivingEndpointRowMapperTest {
    @Test
    void testSlackEndpointMapping() throws SQLException {
        testEndpointMapping(SlackEndpoint.class, SlackEndpoint.JSON_TYPE_IDENTIFIER);
    }

    @Test
    void testTeamsEndpointMapping() throws SQLException {
        testEndpointMapping(TeamsEndpoint.class, TeamsEndpoint.JSON_TYPE_IDENTIFIER);
    }

    private <T> void testEndpointMapping(Class<T> clazz, String endpointId) throws SQLException {
        ResultSet resultSet = Mockito.mock(ResultSet.class);

        UUID uuid = UUID.randomUUID();
        String name = "endpoint-name";

        Mockito.when(resultSet.getString("endpoint_type")).thenReturn(endpointId);
        Mockito.when(resultSet.getString("endpoint_name")).thenReturn(name);
        Mockito.when(resultSet.getObject("id")).thenReturn(uuid);

        ReceivingEndpoint endpoint = new ReceivingEndpointRowMapper().mapRow(resultSet, 0);
        Assertions.assertNotNull(endpoint);
        Assertions.assertTrue(clazz.isInstance(endpoint));
        Assertions.assertEquals(uuid, endpoint.getId());
        Assertions.assertEquals(name, endpoint.getEndpointId());
    }
}
