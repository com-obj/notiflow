package com.obj.nc.services;

import com.obj.nc.domain.dto.EndpointDto;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.repositories.EndpointsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;

@Service
@RequiredArgsConstructor
public class EndpointsServiceImpl implements EndpointsService {
    
    private final EndpointsRepository endpointsRepository;
    private final DeliveryInfoRepository deliveryInfoRepository;
    
    @Override
    public void persistEndpointIfNotExists(RecievingEndpoint endpoint) {
        endpointsRepository.persistEndpointIfNotExists(
                endpoint.getId(),
                endpoint.getEndpointId(),
                endpoint.getEndpointType());
    }
    
    @Override
    public Page<EndpointDto> findAllEndpoints(Instant startAt,
                                              Instant endAt,
                                              EndpointDto.EndpointType endpointType,
                                              Pageable pageable) {
        Page<RecievingEndpoint> endpointsPage = endpointsRepository.findAllEndpoints(startAt, endAt, endpointType, pageable);
        
        return endpointsPage
                .map(receivingEndpoint -> EndpointDto.from(
                        receivingEndpoint, 
                        countMessagesSentToEndpoint(receivingEndpoint)));
    }
    
    @Override
    public List<RecievingEndpoint> findEndpointsByIds(UUID... endpointIds) {
        return endpointsRepository.findEndpointsByIds(asList(endpointIds));
    }
    
    private long countMessagesSentToEndpoint(RecievingEndpoint receivingEndpoint) {
        return deliveryInfoRepository.countByEndpointIdAndStatus(receivingEndpoint.getId(), DELIVERY_STATUS.SENT);
    }
    
}
