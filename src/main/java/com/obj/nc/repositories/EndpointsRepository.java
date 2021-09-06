package com.obj.nc.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.repositories.mappers.ReceivingEndpointRowMapper;

public interface EndpointsRepository extends PagingAndSortingRepository<ReceivingEndpoint, UUID>, EndpointsRepositoryCustom {
    
    @Override
    @Query(value = "SELECT id, endpoint_name, endpoint_type FROM nc_endpoint", 
           rowMapperClass = ReceivingEndpointRowMapper.class)
    List<ReceivingEndpoint> findAll();
    
}
