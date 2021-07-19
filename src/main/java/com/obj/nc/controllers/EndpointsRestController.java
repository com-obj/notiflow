package com.obj.nc.controllers;

import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.repositories.EndpointsRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@RestController
@RequestMapping("/endpoints")
@RequiredArgsConstructor
public class EndpointsRestController {
    private final EndpointsRepository endpointRepo;
    
    @GetMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public List<EndpointDto> findAllEndpoints() {
        List<RecievingEndpoint> receivingEndpoints = endpointRepo.findAll();
        List<EndpointDto> endpointDtos = EndpointDto.createFrom(receivingEndpoints);
        return endpointDtos;
    }
    
    @Data
    @Builder
    public static class EndpointDto {
        private UUID id;
        private String name;
        private String type;
    
        public static List<EndpointDto> createFrom(List<RecievingEndpoint> receivingEndpoints) {
            return receivingEndpoints.stream()
                    .map(
                            receivingEndpoint -> EndpointDto.builder()
                                .id(receivingEndpoint.getId())
                                .name(receivingEndpoint.getEndpointId())
                                .type(receivingEndpoint.getEndpointType())
                                .build()
                    )
                    .collect(Collectors.toList());
        }
    }
}
