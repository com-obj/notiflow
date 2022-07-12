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

import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface DeliveryInfoRepository extends PagingAndSortingRepository<DeliveryInfo, UUID> {

    @Query("select di.* " +
            "from nc_delivery_info di " +
            "join nc_message m " +
            "on di.message_id = m.id " +
            "where di.status = :status " +
            "and (:eventId)::uuid = ANY(m.previous_event_ids) " +
            "order by processed_on")
    List<DeliveryInfo> findByEventIdAndStatusOrderByProcessedOn(
            @Param("eventId") UUID eventId, 
            @Param("status") DELIVERY_STATUS status);

    @Query("select di.* " +
            "from nc_delivery_info di " +
            "join nc_message m " +
            "on di.message_id = m.id " +
            "where di.status = :status " +
            "and (:intentId)::uuid = ANY(m.previous_intent_ids) " +
            "order by processed_on")
    List<DeliveryInfo> findByIntentIdAndStatusOrderByProcessedOn(
            @Param("intentId") UUID intentId, 
            @Param("status") DELIVERY_STATUS status);            

    @Query("select di.* " +
            "from nc_delivery_info di " +
            "join nc_message m " +
            "on di.message_id = m.id " +
            "where (:eventId)::uuid = ANY(m.previous_event_ids) " +
            "order by processed_on")
    List<DeliveryInfo> findByEventIdOrderByProcessedOn(@Param("eventId") UUID eventId);

    List<DeliveryInfo> findByStatus(DELIVERY_STATUS status);

    @Query("select di.* " +
            "from nc_delivery_info di " +
            "join nc_message m " +
            "on di.message_id = m.id " +
            "where :eventId = ANY(m.previous_event_ids) " +
            "and ((:endpointId)::uuid is null or endpoint_id = (:endpointId)::uuid) " +
            "order by processed_on")
    List<DeliveryInfo> findByEventIdAndEndpointIdOrderByProcessedOn(@Param("eventId") UUID eventId,
                                                                    @Param("endpointId") UUID endpointId);

    String QRY_LATEST_DELIVERY_INFO_BY_ENDPOINT_ID = "select di.*\n" +
            "from nc_delivery_info di\n" +
            "         join nc_message m on di.message_id = m.id\n" +
            "where :eventId = ANY (m.previous_event_ids)\n" +
            "  and ((:endpointId)::uuid is null or endpoint_id = (:endpointId)::uuid)\n" +
            "    and (status = 'SENT' OR status = 'FAILED')\n" +
            "limit :size offset :offset";

    @Query(QRY_LATEST_DELIVERY_INFO_BY_ENDPOINT_ID)
    List<DeliveryInfo> findByEventIdAndEndpointIdOrderByProcessedOn(@Param("eventId") UUID eventId,
                                                                    @Param("endpointId") UUID endpointId,
                                                                    @Param("size") int size,
                                                                    @Param("offset") long offset);


    String QRY_COUNT_LATEST_DELIVERY_INFO_BY_ENDPOINT_ID = "select count(*)\n" +
            "from nc_delivery_info di\n" +
            "         join nc_message m on di.message_id = m.id\n" +
            "where :eventId = ANY (m.previous_event_ids)\n" +
            "  and ((:endpointId)::uuid is null or endpoint_id = (:endpointId)::uuid)\n" +
            "    and (status = 'SENT' OR status = 'FAILED');";

    @Query(QRY_COUNT_LATEST_DELIVERY_INFO_BY_ENDPOINT_ID)
    long countByEventIdAndEndpointId(@Param("eventId") UUID eventId, @Param("endpointId") UUID endpointId);


    List<DeliveryInfo> findByEndpointIdOrderByProcessedOn(UUID endpointId);

    @Query("select * " +
            "from nc_delivery_info di " +
            "where di.message_id = any (" +
            "    with recursive msg_chain as ( " +
            "        select msg.id, msg.previous_message_ids " +
            "        from nc_message msg " +
            "        where msg.id = (:messageId) " +
            "    union all " +
            "        select next_msg.id, next_msg.previous_message_ids " +
            "        from nc_message next_msg " +
            "        join msg_chain on msg_chain.id = any ( next_msg.previous_message_ids ))" +
            "    select msg_chain.id from msg_chain)" +
            "order by di.processed_on")
    List<DeliveryInfo> findByMessageIdOrderByProcessedOn(@Param("messageId") UUID messageId);

    @Query("select * " +
            "from nc_delivery_info di " +
            "where di.status = (:status) " +
            "and di.message_id = any (" +
            "    with recursive msg_chain as ( " +
            "        select msg.id, msg.previous_message_ids " +
            "        from nc_message msg " +
            "        where msg.id = (:messageId) " +
            "    union all " +
            "        select next_msg.id, next_msg.previous_message_ids " +
            "        from nc_message next_msg " +
            "        join msg_chain on msg_chain.id = any ( next_msg.previous_message_ids ))" +
            "    select msg_chain.id from msg_chain) " +
            "order by di.processed_on")
    List<DeliveryInfo> findByMessageIdAndStatusOrderByProcessedOn(
                @Param("messageId") UUID messageId,
                @Param("status") DELIVERY_STATUS status);

    @Query("select count(di.id) " +
            "from nc_delivery_info di " +
            "where di.status = (:status) " +
            "and di.message_id = any (" +
            "    with recursive msg_chain as ( " +
            "        select msg.id, msg.previous_message_ids " +
            "        from nc_message msg " +
            "        where msg.id = (:messageId) " +
            "    union all " +
            "        select next_msg.id, next_msg.previous_message_ids " +
            "        from nc_message next_msg " +
            "        join msg_chain on msg_chain.id = any ( next_msg.previous_message_ids ))" +
            "    select msg_chain.id from msg_chain)")
    long countByMessageIdAndStatus(@Param("messageId") UUID messageId,
                                   @Param("status") DELIVERY_STATUS status);

    long countByEndpointIdAndProcessedOnAfter(UUID endpointId, Instant timestamp);
}
