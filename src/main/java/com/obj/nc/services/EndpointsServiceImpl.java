package com.obj.nc.services;

import com.obj.nc.domain.dto.EndpointDto;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.repositories.EndpointsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EndpointsServiceImpl implements EndpointsService {
    
    private final EndpointsRepository endpointsRepository;
    
    @Override
    public void persistEndpointIfNotExists(RecievingEndpoint endpoint) {
        endpointsRepository.persistEnpointIfNotExists(endpoint);
    }
    
    @Override
    public Page<EndpointDto> findAllEndpoints(Instant startAt,
                                              Instant endAt,
                                              EndpointDto.EndpointType endpointType,
                                              Pageable pageable) {
        return endpointsRepository.findAllEndpoints(startAt, endAt, endpointType, pageable);
    }
    
    @Override
    public List<RecievingEndpoint> findEndpointsByIds(UUID... endpointIds) {
        return endpointsRepository.findByIds(endpointIds);
    }
    
}
