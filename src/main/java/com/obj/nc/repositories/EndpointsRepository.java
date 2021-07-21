package com.obj.nc.repositories;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;


public interface EndpointsRepository extends PagingAndSortingRepository<RecievingEndpoint, UUID> {
    
    @Modifying
    @Query("insert into nc_endpoint " +
           "(id, endpoint_name, endpoint_type) " +
           "values " +
           "(:id, :endpoint_name, :endpoint_type) " +
           "ON CONFLICT ON CONSTRAINT nc_endpoint_endpoint_id_unique_key DO NOTHING")
    void persistEndpointIfNotExists(@Param("id") UUID id, @Param("endpoint_name") String name, @Param("endpoint_type") String type);
    
    @Query(value = "select id, endpoint_name, endpoint_type " + 
                   "from nc_endpoint " +
                   "where id in (:endpoint_ids)", rowMapperClass = FindEndpointsRowMapper.class)
    List<RecievingEndpoint> findEndpointsByIds(Pageable pageable, @Param("endpoint_ids") List<UUID> endpointIds);
    
    @Query(value = "select count(*) from nc_endpoint where id in (:endpoint_ids)")
    long countAllEndpointsByIds(@Param("endpoint_ids") List<UUID> endpointIds);
    
    @Query(value = "select id, endpoint_name, endpoint_type " +
                   "from nc_endpoint", rowMapperClass = FindEndpointsRowMapper.class)
    List<RecievingEndpoint> findAllEndpoints(Pageable pageable);
    
    @Query(value = "select count(*) from nc_endpoint")
    long countAllEndpoints();
    
    default void persistEndpointIfNotExists(RecievingEndpoint endpoint) {
        persistEndpointIfNotExists(endpoint.getId(), endpoint.getEndpointId(), endpoint.getEndpointType());
    }
    
    default void persistEndpointsIfNotExists(RecievingEndpoint ... endpoints) {
        asList(endpoints)
                .forEach(this::persistEndpointIfNotExists);
    }
    
    default List<RecievingEndpoint> findEndpointsByIds(UUID ... endpointIds) {
        return findEndpointsByIds(null, asList(endpointIds));
    }
    
    class FindEndpointsRowMapper implements RowMapper<RecievingEndpoint> {
        @Override
        public RecievingEndpoint mapRow(ResultSet resultSet, int i) throws SQLException {
            RecievingEndpoint endpoint = null;
            
            String epType = resultSet.getString("endpoint_type");
            if (EmailEndpoint.JSON_TYPE_IDENTIFIER.equals(epType)) {
                endpoint = new EmailEndpoint(resultSet.getString("endpoint_name"));
            } else if (SmsEndpoint.JSON_TYPE_IDENTIFIER.equals(epType)) {
                endpoint = new SmsEndpoint(resultSet.getString("endpoint_name"));
            } else {
                throw new RuntimeException("Uknown endpoint type for EndpointsRepository: " + epType);
            }
    
            endpoint.setId((UUID) resultSet.getObject("id"));
            return endpoint;
        }
    }
    
}
