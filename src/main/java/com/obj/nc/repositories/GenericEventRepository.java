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

import com.obj.nc.domain.dto.DeliveryStatsByEndpointType;
import com.obj.nc.domain.dto.DeliveryStatsByEndpointType.DeliveryStatsByEndpointTypeRowMapper;
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
					"	count(distinct di.id) filter(where di.status = 'FAILED') as messages_failed_count, " +
					"	count(distinct di.id) filter(where di.status = 'DELIVERED') as messages_delivered_count, " +
					"	count(distinct di.id) filter(where di.status = 'DELIVERY_PENDING') as messages_delivery_pending_count, " +
					"	count(distinct di.id) filter(where di.status = 'DELIVERY_UNKNOWN') as messages_delivery_unknown_count, " +
					"	count(distinct di.id) filter(where di.status = 'DELIVERY_FAILED') as messages_delivery_failed_count, " +
					"	count(distinct di.id) filter(where di.status = 'PROCESSING') as messages_processing_count, " +
					"	count(distinct di.id) filter(where di.status = 'DISCARDED') as messages_discarded_count " +
					"from " +
					"	nc_event event " +
					"left join " +
					"	nc_intent intent on event.id = any ( intent.previous_event_ids ) " +
					"left join ( " +
					"	select msg.*, m2e.endpoint_id as endpoint_id " +
					"	from nc_message msg " +
					"	inner join nc_message_2_endpoint_rel m2e on m2e.message_id = msg.id " +
					") message on event.id = any ( message.previous_event_ids ) " +
					"left join " +
					"	nc_delivery_info di on di.message_id = message.id " +
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
		value =
			"with latest_msg_di as (\n" +
			"    select di.message_id as message_id, di.status, MAX(di.processed_on) as processed_on\n" +
			"    from nc_delivery_info di\n" +
			"    join nc_message m on di.message_id = m.id\n" +
			"    where di.status != 'PROCESSING'\n" +
			"    GROUP BY di.message_id, di.status\n" +
			")\n" +
			"select msg_with_endp.endpoint_type,\n" +
			"       count(msg_with_endp.message_id)                                    as messages_count,\n" +
			"       count(distinct msg_with_endp.endpoint_id)                          as endpoints_count,\n" +
			"       count(di.message_id) filter (where di.status = 'SENT')             as messages_sent_count,\n" +
			"       count(di.message_id) filter (where di.status = 'READ')             as messages_read_count,\n" +
			"       count(di.message_id) filter (where di.status = 'FAILED')           as messages_failed_count,\n" +
			"       count(di.message_id) filter (where di.status = 'DELIVERED')        as messages_delivered_count,\n" +
			"       count(di.message_id) filter (where di.status = 'DELIVERY_PENDING') as messages_delivery_pending_count,\n" +
			"       count(di.message_id) filter (where di.status = 'DELIVERY_UNKNOWN') as messages_delivery_unknown_count,\n" +
			"       count(di.message_id) filter (where di.status = 'DELIVERY_FAILED')  as messages_delivery_failed_count,\n" +
			"       count(di.message_id) filter (where di.status = 'DISCARDED')        as messages_discarded_count\n" +
			"from nc_event e\n" +
			"    left join (select msg.*, m2e.message_id as message_id, m2e.endpoint_id as endpoint_id, ep.endpoint_type\n" +
			"        from nc_message msg\n" +
			"            inner join nc_message_2_endpoint_rel m2e on (m2e.message_id = msg.id)\n" +
			"            inner join nc_endpoint ep on ep.id = m2e.endpoint_id) msg_with_endp\n" +
			"        on (e.id = any (msg_with_endp.previous_event_ids))\n" +
			"    left join latest_msg_di di on di.message_id = msg_with_endp.id\n" +
			"where e.id = (:eventId)::uuid\n" +
			"  and message_class NOT LIKE '%Templated'\n" +
			"group by msg_with_endp.endpoint_type",
		rowMapperClass = DeliveryStatsByEndpointTypeRowMapper.class
	)
	List<DeliveryStatsByEndpointType> findEventStatsByEndpointType(@Param("eventId") UUID eventId);
	
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

	// target event for summary notification is either too old in terms of latest delivery info or sending of all its messages has been completed
	// in these cases the delivery info of any event's message shouldn't change anymore
	@Query(
		"select e.id,e.flow_id,e.external_id, e.payload_json,e.time_created,e.time_consumed, e.payload_type, e.notify_after_processing, e.name, e.description\n" +
		"from nc_event e\n" +
		"    join nc_message m on (e.id  = ANY(m.previous_event_ids))\n" +
		"    join nc_delivery_info di on (di.message_id = m.id )\n" +
		"where notify_after_processing = true\n" +
		"group by 1,2,3,4,5,6,7,8,9,10\n" +
		"having max(di.processed_on) < :timestamp\n" +
		"union (\n" +
		"    with latest_msg_di as (\n" +
		"        select di.message_id as message_id, di.status, MAX(di.processed_on) as processed_on\n" +
		"        from nc_delivery_info di\n" +
		"                 join nc_message m on di.message_id = m.id\n" +
		"        where di.status != 'PROCESSING'\n" +
		"        GROUP BY di.message_id, di.status\n" +
		"    )\n" +
		"    select e.id,e.flow_id,e.external_id, e.payload_json,e.time_created,e.time_consumed, e.payload_type, e.notify_after_processing, e.name, e.description\n" +
		"    from nc_event e\n" +
		"             join nc_message m on (e.id  = ANY(m.previous_event_ids))\n" +
		"             join latest_msg_di di on (di.message_id = m.id )\n" +
		"    where notify_after_processing = true\n" +
		"    group by 1,2,3,4,5,6,7,8,9,10\n" +
		"    having max(di.processed_on) < NOW() - make_interval(secs  => :secondsSinceLastProcessing)\n" +
			"and count(di.message_id) filter (where di.status != 'DELIVERY_PENDING') = count(di.message_id)\n" +
		")\n"
	)
	List<GenericEvent> findEventsForSummaryNotification(@Param("secondsSinceLastProcessing") int secondsSinceLastProcessing,
														@Param("timestamp") Instant timestamp);
}
