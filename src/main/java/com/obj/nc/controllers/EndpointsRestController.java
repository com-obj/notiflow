/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.controllers;

import com.obj.nc.config.PagingConfigProperties;
import com.obj.nc.domain.dto.fe.EndpointDetailDto;
import com.obj.nc.domain.dto.fe.EndpointDetailDto.EndpointType;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.pagination.ResultPage;
import com.obj.nc.repositories.EndpointsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.obj.nc.utils.PagingUtils.createPageRequest;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@RestController
@RequestMapping("/endpoints")
@RequiredArgsConstructor
public class EndpointsRestController {
    
    private final EndpointsRepository endpointsRepository;
    private final PagingConfigProperties pagingConfigProperties;
    
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Page<EndpointDetailDto> findAllEndpoints(@RequestParam(value = "processedFrom", required = false, defaultValue = "2000-01-01T12:00:00Z")
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant processedFrom,
                                                    @RequestParam(value = "processedTo", required = false, defaultValue = "2100-01-01T12:00:00Z")
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant processedTo,
                                                    @RequestParam(value = "endpointType", required = false) EndpointType endpointType,
                                                    @RequestParam(value = "eventId", required = false) UUID eventId,
                                                    @RequestParam(value = "endpointId", required = false) UUID endpointId,
                                                    @RequestParam("page") int page,
                                                    @RequestParam("size") int size) {
        Pageable pageable = createPageRequest(page, size, pagingConfigProperties);
        List<EndpointDetailDto> endpoints = endpointsRepository
                .findAllEndpointsWithStats(processedFrom, processedTo, endpointType, eventId, endpointId, pageable.getOffset(), pageable.getPageSize())
                .stream()
                .map(EndpointDetailDto::from)
                .collect(Collectors.toList());
        
        long endpointsTotalCount = endpointsRepository.countAllEndpointsWithStats(processedFrom, processedTo, endpointType, eventId, endpointId);
        
        return new ResultPage<>(endpoints, pageable, endpointsTotalCount);
    }
    
    @GetMapping(value = "/{endpointId}", produces = APPLICATION_JSON_VALUE)
    public ReceivingEndpoint findEndpointById(@PathVariable("endpointId") String endpointId) {
        return endpointsRepository
                .findEndpointById(UUID.fromString(endpointId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    
}
