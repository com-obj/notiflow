package com.obj.nc.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.repositories.mappers.ReceivingEndpointRowMapper;
import org.springframework.data.repository.query.Param;

public interface EndpointsRepository extends PagingAndSortingRepository<ReceivingEndpoint, UUID>, EndpointsRepositoryCustom {
    
    @Query(
            value = "select id, endpoint_name, endpoint_type " + 
                    "from nc_endpoint",
           rowMapperClass = ReceivingEndpointRowMapper.class)
    List<ReceivingEndpoint> findAllEndpoints();
    
    @Query(
            value = "select id, endpoint_name, endpoint_type " + 
                    "from nc_endpoint " +
                    "where id = (:endpointId)",
            rowMapperClass = ReceivingEndpointRowMapper.class)
    Optional<ReceivingEndpoint> findEndpointById(@Param("endpointId") UUID endpointId);
    
}
