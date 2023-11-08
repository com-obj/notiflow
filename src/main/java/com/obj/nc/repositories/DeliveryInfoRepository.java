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

import com.obj.nc.domain.dto.DeliveryInfoDto;
import com.obj.nc.domain.dto.DeliveryInfoDto.DeliveryInfoDtoMapper;
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

    String WITH_LATEST_DELIVERY_INFO_BY_ENDPOINT_ID =
        "with latest_msg_di as (\n" +
        "    select di.status, di.message_id as message_id, MAX(di.processed_on) as processed_on\n" +
        "    from nc_delivery_info di join nc_message m on di.message_id = m.id\n" +
        "    where :eventId = ANY (m.previous_event_ids)\n" +
        "    and ((:endpointId)::uuid is null or endpoint_id = (:endpointId)::uuid)\n" +
        "    and di.status IN ('SENT', 'FAILED', 'DELIVERED', 'DELIVERY_UNKNOWN', 'DELIVERY_FAILED', 'DELIVERY_PENDING')\n" +
        "    GROUP BY di.status, di.message_id\n" +
        ")\n";

    String QRY_LATEST_DELIVERY_INFO_BY_ENDPOINT_ID = WITH_LATEST_DELIVERY_INFO_BY_ENDPOINT_ID +
            "select di.*\n" +
            "from nc_delivery_info di\n" +
            "join latest_msg_di latest on latest.message_id = di.message_id and latest.processed_on = di.processed_on and latest.status = di.status\n" +
            "order by processed_on\n" +
            "limit :size offset :offset";

    @Query(QRY_LATEST_DELIVERY_INFO_BY_ENDPOINT_ID)
    List<DeliveryInfo> findLatestByEventIdAndEndpointIdOrderByProcessedOn(@Param("eventId") UUID eventId,
                                                                          @Param("endpointId") UUID endpointId,
                                                                          @Param("size") int size,
                                                                          @Param("offset") long offset);

    String QRY_COUNT_LATEST_DELIVERY_INFO_BY_ENDPOINT_ID
            = WITH_LATEST_DELIVERY_INFO_BY_ENDPOINT_ID + "select count(1) from latest_msg_di";

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

    /*
    * NOTICE
    * - this method is used for finding messages in non-terminal state so that we can ask external system for its current state
    * "NOT EXISTS" part filters messages, which are already in terminal state - because we INSERT (instead of UPDATE) new deliveryInfo when message delivery state changes,
    * we store multiple deliveryInfos per message, so we try to find such message for which doesn't exist terminal state deliveryInfo
    * - in "where di.status IN ('SENT', 'DELIVERY_PENDING')" statement there isn't PROCESSING state because we haven't sent the message yet, so
    * asking external system for its state wouldn't make sense
    * */
    @Query(
        value = "select distinct on (m.id)" +
                "    di.id AS delivery_id, " +
                "    m.id AS message_id, " +
                "    e.endpoint_type AS endpoint_type, " +
                "    di.status AS delivery_status, " +
                "    di.additional_information AS additional_information, " +
                "    m.reference_number AS reference_number " +
                "from nc_delivery_info di " +
                "join nc_message m " +
                "on di.message_id = m.id " +
                "join nc_endpoint e " +
                "on di.endpoint_id = e.id " +
                "where di.status IN ('SENT', 'DELIVERY_PENDING') " +
                "and e.endpoint_type IN (:endpointTypes) " +
                "and di.processed_on > :timestamp " +
                "and not exists ( " +
                "   select 1 " +
                "   from nc_delivery_info di2 " +
                "   where di.message_id = di2.message_id " +
                "   and di2.status NOT IN ('PROCESSING', 'SENT', 'DELIVERY_PENDING')" +
                ") " +
                "order by m.id, di.processed_on desc",
        rowMapperClass = DeliveryInfoDtoMapper.class
    )
    List<DeliveryInfoDto> findUnfinishedDeliveriesNotOlderThan(
            @Param("timestamp") Instant timestamp,
            @Param("endpointTypes") List<String> endpointTypes
    );

}
