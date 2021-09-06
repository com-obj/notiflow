package com.obj.nc.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.obj.nc.domain.dto.EndpointDto;
import com.obj.nc.domain.dto.EndpointDto.EndpointType;
import com.obj.nc.repositories.EndpointsRepository;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/endpoints")
@RequiredArgsConstructor
public class EndpointsRestController {
    
    private final EndpointsRepository endpointsRepository;
    
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Page<EndpointDto> findAllEndpoints(
            @RequestParam(value = "startAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startAt,
            @RequestParam(value = "endAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endAt,
            @RequestParam(value = "endpointType", required = false, defaultValue = "ANY") EndpointType endpointType,
            Pageable pageable) {
        return endpointsRepository.findAllEndpoints(startAt, endAt, endpointType, pageable);
    }
    
}
