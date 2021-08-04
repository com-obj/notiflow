package com.obj.nc.repositories;

import com.obj.nc.domain.dto.EndpointDto;
import com.obj.nc.domain.dto.EndpointDto.EndpointType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface EndpointsRepositoryCustom {
    
    Page<EndpointDto> findAllEndpoints(Instant startAt,
                                       Instant endAt,
                                       EndpointType endpointType,
                                       Pageable pageable);
    
}