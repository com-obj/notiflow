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
import java.util.Optional;
import java.util.UUID;

import com.obj.nc.domain.dto.EndpointTableViewDto.EndpointType;
import com.obj.nc.domain.endpoints.ReceivingEndpointWithStats;
import com.obj.nc.domain.endpoints.ReceivingEndpointWithStats.ReceivingEndpointWithStatsRowMapper;
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
    
    @Query(
            value = "select " +
                    "   ep.id, " +
                    "   ep.endpoint_name, " +
                    "   ep.endpoint_type, " +
                    "	count(distinct event_id) as events_count, " +
//                    "	count(distinct intent_id) as intents_count, " + // TODO uncomment when nc_intent stores endpoint_ids
                    "	count(distinct msg.id) as messages_count, " +
                    "	count(distinct ep.id) as endpoints_count, " +
                    "	count(distinct di.id) filter(where di.status = 'SENT') as messages_sent_count, " +
                    "	count(distinct di.id) filter(where di.status = 'READ') as messages_read_count, " +
                    "	count(distinct di.id) filter(where di.status = 'FAILED') as messages_failed_count " +
                    "from " +
                    "	nc_endpoint ep " +
                    "left join ( " +
                    "	select msg.id, msg.endpoint_ids, event_id " +
                    "	from nc_message msg " +
                    "	cross join unnest(msg.previous_event_ids) as event_id " +
                    ") msg on ep.id = any ( msg.endpoint_ids ) " +
                    // TODO uncomment when nc_intent stores endpoint_ids
//                    "left join ( " +
//                    "	select intent.id, intent.endpoint_ids, intent_id " +
//                    "	from nc_intent intent " +
//                    "	cross join unnest(intent.previous_intent_ids) as intent_id " +
//                    ") intent on ep.id = any ( intent.endpoint_ids ) " +
                    "left join (" +
                    "   select di.id, di.endpoint_id, di.status " +
                    "   from nc_delivery_info di " +
                    "	where " +
                    "       processed_on between :processedFrom and :processedTo " +
                    "   and " +
                    "       di.message_id is not null " +
                    ") di on ep.id = di.endpoint_id " +
                    "where " +
                    "   (:endpointType::varchar is null or ep.endpoint_type = :endpointType::varchar) " +
                    "and " +
                    "   (:eventId::uuid is null or event_id = :eventId::uuid) " +
                    "and " +
                    "	(:endpointId::uuid is null or ep.id = :endpointId::uuid) " +
                    "group by ep.id, ep.endpoint_name , ep.endpoint_type " +
                    "offset :offset rows fetch next :pageSize rows only",
            rowMapperClass = ReceivingEndpointWithStatsRowMapper.class)
    List<ReceivingEndpointWithStats> findAllEndpointsWithStats(@Param("processedFrom") Instant processedFrom, 
                                                               @Param("processedTo") Instant processedTo, 
                                                               @Param("endpointType") EndpointType endpointType, 
                                                               @Param("eventId") UUID eventId, 
                                                               @Param("endpointId") UUID endpointId, 
                                                               @Param("offset") long offset, 
                                                               @Param("pageSize") int pageSize);
    
    @Query(
            value = "select " +
                    "	count(distinct ep.id) " +
                    "from " +
                    "	nc_endpoint ep " +
                    "left join ( " +
                    "	select msg.id, msg.endpoint_ids, event_id " +
                    "	from nc_message msg " +
                    "	cross join unnest(previous_event_ids) as event_id " +
                    ") msg on ep.id = any ( msg.endpoint_ids ) " +
                    // TODO uncomment when nc_intent stores endpoint_ids
//                    "left join ( " +
//                    "	select intent.id, intent.endpoint_ids, intent_id " +
//                    "	from nc_intent intent " +
//                    "	cross join unnest(intent.previous_intent_ids) as intent_id " +
//                    ") intent on ep.id = any ( intent.endpoint_ids ) " +
                    "left join (" +
                    "   select di.id, di.endpoint_id, di.status " +
                    "   from nc_delivery_info di " +
                    "	where processed_on between :processedFrom and :processedTo" +
                    ") di on ep.id = di.endpoint_id " +
                    "where " +
                    "   (:endpointType::varchar is null or ep.endpoint_type = :endpointType::varchar) " +
                    "and " +
                    "   (:eventId::uuid is null or event_id = :eventId::uuid) " +
                    "and " +
                    "	(:endpointId::uuid is null or ep.id = :endpointId::uuid) ")
    long countAllEndpointsWithStats(@Param("processedFrom") Instant processedFrom, 
                                    @Param("processedTo") Instant processedTo, 
                                    @Param("endpointType") EndpointType endpointType,
                                    @Param("eventId") UUID eventId, 
                                    @Param("endpointId") UUID endpointId);
    
}
