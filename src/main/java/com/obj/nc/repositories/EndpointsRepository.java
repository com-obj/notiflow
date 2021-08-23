package com.obj.nc.repositories;

import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.repositories.mappers.ReceivingEndpointRowMapper;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.UUID;

public interface EndpointsRepository extends PagingAndSortingRepository<RecievingEndpoint, UUID>, EndpointsRepositoryCustom {
    
    @Override
    @Query(value = "SELECT id, endpoint_name, endpoint_type FROM nc_endpoint", 
           rowMapperClass = ReceivingEndpointRowMapper.class)
    List<RecievingEndpoint> findAll();
    
}
