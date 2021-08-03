package com.obj.nc.services;

import com.obj.nc.domain.dto.EndpointDto;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.obj.nc.domain.dto.EndpointDto.*;

public interface EndpointsService {
    
    void persistEndpointIfNotExists(RecievingEndpoint endpoint);
    
    Page<EndpointDto> findAllEndpoints(Instant startAt,
                                       Instant endAt,
                                       EndpointType endpointType, 
                                       Pageable pageable);
    
    List<RecievingEndpoint> findEndpointsByIds(Pageable pageable,
                                               UUID... endpointIds);
    
}
