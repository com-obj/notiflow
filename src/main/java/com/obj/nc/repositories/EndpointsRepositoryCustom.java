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

package com.obj.nc.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.obj.nc.domain.dto.EndpointTableViewDto;
import com.obj.nc.domain.dto.EndpointTableViewDto.EndpointType;
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
    
    Page<EndpointTableViewDto> findAllEndpoints(Instant startAt,
                                                Instant endAt,
                                                EndpointType endpointType,
                                                String eventId,
                                                Pageable pageable);
    
}