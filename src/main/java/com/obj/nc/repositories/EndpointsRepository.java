package com.obj.nc.repositories;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.repositories.mappers.ReceivingEndpointRowMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;


public interface EndpointsRepository extends PagingAndSortingRepository<RecievingEndpoint, UUID>, EndpointsRepositoryCustom {
    
    @Modifying
    @Query("insert into nc_endpoint " +
           "(id, endpoint_name, endpoint_type) " +
           "values " +
           "(:id, :endpoint_name, :endpoint_type) " +
           "ON CONFLICT ON CONSTRAINT nc_endpoint_endpoint_id_unique_key DO NOTHING")
    void persistEndpointIfNotExists(@Param("id") UUID id,
                                    @Param("endpoint_name") String name,
                                    @Param("endpoint_type") String type);
    
    @Query(value = "select id, endpoint_name, endpoint_type " + 
                   "from nc_endpoint " +
                   "where id in (:endpoint_ids)", rowMapperClass = ReceivingEndpointRowMapper.class)
    List<RecievingEndpoint> findEndpointsByIds(@Param("endpoint_ids") List<UUID> endpointIds,
                                               Pageable pageable);
    
}
