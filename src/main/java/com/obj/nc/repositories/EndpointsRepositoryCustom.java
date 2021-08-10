package com.obj.nc.repositories;

import com.obj.nc.domain.dto.EndpointDto;
import com.obj.nc.domain.dto.EndpointDto.EndpointType;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.refIntegrity.EntityExistanceChecker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface EndpointsRepositoryCustom extends EntityExistanceChecker<UUID> {
    
    <T extends RecievingEndpoint> List<T> persistEnpointIfNotExists(List<T> toPersist);
    
    Map<String, RecievingEndpoint> persistEnpointIfNotExistsMappedToNameId(List<RecievingEndpoint> toPersist);
    
    <T extends RecievingEndpoint> List<T> findExistingEndpointsByNameId(List<T> ednpoints);
    
    <T extends RecievingEndpoint> List<T> findExistingEndpointsByIsNewFlag(List<T> ednpoints);
    
    List<RecievingEndpoint> findByIds(UUID... endpointIds);
    
    List<RecievingEndpoint> findByNameIds(String ... endpointNames);
    
    <T extends RecievingEndpoint> T persistEnpointIfNotExists(T ednpoint);
    
    List<RecievingEndpoint> persistEnpointIfNotExists(RecievingEndpoint ... ednpoints);
    
    Map<String, RecievingEndpoint> persistEnpointIfNotExistsMappedToNameId(RecievingEndpoint ... ednpoints);
    
    Page<EndpointDto> findAllEndpoints(Instant startAt,
                                       Instant endAt,
                                       EndpointType endpointType,
                                       Pageable pageable);
    
}