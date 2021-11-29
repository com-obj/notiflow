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

import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.event.GenericEventWithStats;
import com.obj.nc.domain.event.GenericEventWithStats.GenericEventWithStatsRowMapper;
import com.obj.nc.domain.refIntegrity.EntityExistenceChecker;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface GenericEventRepository extends PagingAndSortingRepository<GenericEvent, UUID>, EntityExistenceChecker<UUID> {
	
	GenericEvent findFirstByTimeConsumedIsNullOrderByTimeCreatedAsc();
	
	GenericEvent findByExternalId(String externalId);
	
	@Query(
			value = "select " +
					"	event.*, " +
					"	count(distinct event.id) as events_count, " +
					"	count(distinct intent.id) as intents_count, " +
					"	count(distinct message.id) as messages_count, " +
					"	count(distinct message.endpoint_id) as endpoints_count, " +
					"	count(distinct di.id) filter(where di.status = 'SENT') as messages_sent_count, " +
					"	count(distinct di.id) filter(where di.status = 'READ') as messages_read_count, " +
					"	count(distinct di.id) filter(where di.status = 'FAILED') as messages_failed_count " +
					"from " +
					"	nc_event event " +
					"left join " +
					"	nc_intent intent on event.id = any ( intent.previous_event_ids ) " +
					"left join ( " +
					"	select msg.*, d.endpoint_id as endpoint_id " +
					"	from nc_message msg " +
					"	inner join nc_delivery_options d on d.message_id = msg.id " +
					") message on event.id = any ( message.previous_event_ids ) " +
					"left join " +
					"	nc_delivery_info di on di.event_id = event.id " +
					"where " +
					"	event.time_consumed between (:consumedFrom) and (:consumedTo) " +
					"and " +
					"	(:eventId)::uuid is null or event.id = (:eventId)::uuid " +
					"group by " +
					"	event.id, " +
					"	event.flow_id, " +
					"	event.external_id, " +
					"	event.payload_json, " +
					"	event.time_created, " +
					"	event.time_consumed, " +
					"	event.payload_type " +
					"offset :offset rows fetch next :pageSize rows only", 
			rowMapperClass = GenericEventWithStatsRowMapper.class)
	List<GenericEventWithStats> findAllEventsWithStats(@Param("consumedFrom") Instant consumedFrom,
													   @Param("consumedTo") Instant consumedTo,
													   @Param("eventId") UUID eventId,
													   @Param("offset") long offset,
													   @Param("pageSize") int pageSize);
	
	@Query(
			value = "select " +
					"	count(event.id) " +
					"from " +
					"	nc_event event " +
					"where " +
					"	event.time_consumed between (:consumedFrom) and (:consumedTo) " +
					"and " +
					"	(:eventId)::uuid is null or event.id = (:eventId)::uuid")
	long countAllEventsWithStats(@Param("consumedFrom") Instant consumedFrom,
								 @Param("consumedTo") Instant consumedTo,
								 @Param("eventId") UUID eventId);

}
