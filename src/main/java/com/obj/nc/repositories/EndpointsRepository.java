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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.repositories.mappers.ReceivingEndpointRowMapper;
import org.springframework.data.repository.query.Param;

public interface EndpointsRepository extends PagingAndSortingRepository<ReceivingEndpoint, UUID>, EndpointsRepositoryCustom {
    
    @Query(
            value = "select id, endpoint_name, endpoint_type " + 
                    "from nc_endpoint",
           rowMapperClass = ReceivingEndpointRowMapper.class)
    List<ReceivingEndpoint> findAllEndpoints();
    
    @Query(
            value = "select id, endpoint_name, endpoint_type " + 
                    "from nc_endpoint " +
                    "where id = (:endpointId)",
            rowMapperClass = ReceivingEndpointRowMapper.class)
    Optional<ReceivingEndpoint> findEndpointById(@Param("endpointId") UUID endpointId);
    
}
