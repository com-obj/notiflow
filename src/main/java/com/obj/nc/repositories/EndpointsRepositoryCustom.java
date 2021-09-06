package com.obj.nc.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.obj.nc.domain.dto.EndpointDto;
import com.obj.nc.domain.dto.EndpointDto.EndpointType;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.refIntegrity.EntityExistenceChecker;

public interface EndpointsRepositoryCustom extends EntityExistenceChecker<UUID> {
    
    <T extends ReceivingEndpoint> List<T> persistEnpointIfNotExists(List<T> toPersist);
    
    Map<String, ReceivingEndpoint> persistEnpointIfNotExistsMappedToNameId(List<ReceivingEndpoint> toPersist);
    
    <T extends ReceivingEndpoint> List<T> findExistingEndpointsByNameId(List<T> ednpoints);
    
    <T extends ReceivingEndpoint> List<T> findExistingEndpointsByIsNewFlag(List<T> ednpoints);
    
    List<ReceivingEndpoint> findByIds(UUID... endpointIds);
    
    List<ReceivingEndpoint> findByNameIds(String ... endpointNames);
    
    <T extends ReceivingEndpoint> T persistEnpointIfNotExists(T ednpoint);
    
    List<ReceivingEndpoint> persistEnpointIfNotExists(ReceivingEndpoint ... ednpoints);
    
    Map<String, ReceivingEndpoint> persistEnpointIfNotExistsMappedToNameId(ReceivingEndpoint ... ednpoints);
    
    Page<EndpointDto> findAllEndpoints(Instant startAt,
                                       Instant endAt,
                                       EndpointType endpointType,
                                       Pageable pageable);
    
}