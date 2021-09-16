/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.time.Instant;
import java.util.UUID;

import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.obj.nc.domain.dto.EndpointTableViewDto;
import com.obj.nc.domain.dto.EndpointTableViewDto.EndpointType;
import com.obj.nc.repositories.EndpointsRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

@Validated
@RestController
@RequestMapping("/endpoints")
@RequiredArgsConstructor
public class EndpointsRestController {
    
    private final EndpointsRepository endpointsRepository;
    
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Page<EndpointTableViewDto> findAllEndpoints(@RequestParam(value = "startAt", required = false) 
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startAt, 
                                                       @RequestParam(value = "endAt", required = false)
                                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endAt, 
                                                       @RequestParam(value = "endpointType", required = false, defaultValue = "ANY") EndpointType endpointType,
                                                       @RequestParam(value = "eventId", required = false) String eventId,
                                                       Pageable pageable) {
        return endpointsRepository.findAllEndpoints(startAt, endAt, endpointType, eventId, pageable);
    }
    
    @GetMapping(value = "/{endpointId}", produces = APPLICATION_JSON_VALUE)
    public ReceivingEndpoint findEndpointById(@PathVariable("endpointId") String endpointId) {
        return endpointsRepository
                .findEndpointById(UUID.fromString(endpointId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    
}
