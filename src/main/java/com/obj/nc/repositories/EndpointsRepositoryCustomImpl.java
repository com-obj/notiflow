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

import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.repositories.mappers.ReceivingEndpointRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.sql.Timestamp.from;

@Log4j2
@RequiredArgsConstructor
public class EndpointsRepositoryCustomImpl implements EndpointsRepositoryCustom {
    
    private final JdbcTemplate jdbcTemplate;
    
    /**
     *
     * @param toPersist
     * @return client must use returned list because IDs of endpoints can be different to those provided in input parameter
     */
    @Override
    public <T extends ReceivingEndpoint> List<T> persistEnpointIfNotExists(List<T> toPersist) {
        try  {
            NewAndExistingEndpoints newAndExisting = findNewAndExisting(toPersist);
            
            String inserEndpointIfNotExistsSQL =
                    "insert into nc_endpoint "
                            + "(id, endpoint_name, endpoint_type, time_created) "
                            + "values "
                            + "(?, ?, ?, ?) ";
            
            List<ReceivingEndpoint> toInsert = newAndExisting.newEndpoints;
            List<T> persited = (List<T>)newAndExisting.existingEndpoints;
            
            jdbcTemplate.batchUpdate(
                    inserEndpointIfNotExistsSQL,
                    new BatchPreparedStatementSetter() {
                        
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ReceivingEndpoint endpoint = toInsert.get(i);
                            ps.setObject(1, endpoint.getId());
                            ps.setString(2, endpoint.getEndpointId());
                            ps.setString(3, endpoint.getEndpointType());
                            ps.setTimestamp(4, from(Instant.now()));
                            
                            persited.add((T)endpoint);
                        }
                        
                        public int getBatchSize() {
                            return toInsert.size();
                        }
                        
                    });
            
            return persited;
        } catch (RuntimeException e) {
            log.error(e);
            throw e;
        }
    }
    
    private <T extends ReceivingEndpoint> NewAndExistingEndpoints findNewAndExisting(List<T> toPersist) {
        NewAndExistingEndpoints newAndExisting = new NewAndExistingEndpoints();
        
        List<T> existing = findExistingEndpointsByNameId(toPersist);
        for (ReceivingEndpoint endPoint: toPersist) {
            if (!endPoint.isNew()) {
                newAndExisting.existingEndpoints.add(endPoint);
                continue;
            }
            
            Optional<T> existingEndp = existing
                    .stream()
                    .filter(e -> e.getEndpointId().equals(endPoint.getEndpointId()) 
                            && e.getEndpointType().equals(endPoint.getEndpointType()))
                    .findFirst();
            if (existingEndp.isPresent()) {
                newAndExisting.existingEndpoints.add(existingEndp.get());
                continue;
            }
            
            newAndExisting.newEndpoints.add(endPoint);
        }
        return newAndExisting;
    }
    
    private static class NewAndExistingEndpoints {
        public List<ReceivingEndpoint> existingEndpoints = new ArrayList<ReceivingEndpoint>();
        public List<ReceivingEndpoint> newEndpoints = new ArrayList<ReceivingEndpoint>();
    }
    
    
    @Override
    public Map<String, ReceivingEndpoint> persistEnpointIfNotExistsMappedToNameId(List<ReceivingEndpoint> toPersist) {
        List<ReceivingEndpoint> persited = persistEnpointIfNotExists(toPersist);
        
        return persited.stream().collect(Collectors.toMap(ReceivingEndpoint::getEndpointId, Function.identity()));
    }
    
    
    @Override
    public <T extends ReceivingEndpoint> List<T> findExistingEndpointsByNameId(List<T> ednpoints) {
        List<String> namesList = ednpoints.stream()
                .map(ReceivingEndpoint::getEndpointId)
                .collect(Collectors.toList());
        
        List<T> existing = (List<T>)findByNameIds(namesList.toArray(new String[0]));
        
        return existing;
    }
    
    @Override
    public <T extends ReceivingEndpoint> List<T> findExistingEndpointsByIsNewFlag(List<T> ednpoints) {
        List<T> existing = ednpoints.stream()
                .filter(e -> !e.isNew())
                .collect(Collectors.toList());
        
        return existing;
    }
    
    @Override
    public List<ReceivingEndpoint> findByIds(UUID ... endpointIds) {
        String query =
                "select id, endpoint_name, endpoint_type "
                        + "from nc_endpoint "
                        + "where id in (%s)";
        String inSql = String.join(",", Collections.nCopies(endpointIds.length, "?"));
        query = String.format(query, inSql);
        
        List<ReceivingEndpoint> endpoints = jdbcTemplate.query(
                query,
                new ReceivingEndpointRowMapper(),
                (Object[])endpointIds
        );
        
        return endpoints;
    }
    
    @Override
    public List<ReceivingEndpoint> findByNameIds(String ... endpointNames) {
        if (endpointNames.length==0) {
            return new ArrayList<ReceivingEndpoint>();
        }
        
        String query =
                "select id, endpoint_name, endpoint_type "
                        + "from nc_endpoint "
                        + "where endpoint_name in (%s)";
        String inSql = String.join(",", Collections.nCopies(endpointNames.length, "?"));
        query = String.format(query, inSql);
        
        List<ReceivingEndpoint> endpoints = jdbcTemplate.query(
                query,
                new ReceivingEndpointRowMapper(),
                (Object[])endpointNames
        );
        
        return endpoints;
    }
    
    @Override
    public <T extends ReceivingEndpoint> T persistEnpointIfNotExists(T ednpoint) {
        List<ReceivingEndpoint> ednpoints = Arrays.asList(ednpoint);
        
        ReceivingEndpoint persisted = persistEnpointIfNotExists(ednpoints).iterator().next();
        return (T)persisted;
    }
    
    @Override
    public List<ReceivingEndpoint> persistEnpointIfNotExists(ReceivingEndpoint ... ednpoints) {
        return persistEnpointIfNotExists(Arrays.asList(ednpoints));
    }
    
    @Override
    public Map<String, ReceivingEndpoint> persistEnpointIfNotExistsMappedToNameId(ReceivingEndpoint ... ednpoints) {
        return persistEnpointIfNotExistsMappedToNameId(Arrays.asList(ednpoints));
    }
    
    @Override
    public boolean existsById(UUID id) {
        return jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM nc_endpoint WHERE id = ?)", Boolean.class, id);
    }
    
}
