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

import com.obj.nc.domain.dto.fe.MessageDto;
import com.obj.nc.domain.message.MessagePersistentState;
import com.obj.nc.domain.refIntegrity.EntityExistenceChecker;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends PagingAndSortingRepository<MessagePersistentState, UUID>, EntityExistenceChecker<UUID> {
	
	List<MessagePersistentState> findByIdIn(List<UUID> intentIds);
    
    @Query(
        rowMapperClass = MessageDto.MessageTableViewDtoRowMapper.class, value =
        "select\n" +
        "    msg.id,\n" +
        "    msg.flow_id,\n" +
        "    msg.time_created,\n" +
        "    (\n" +
        "        select distinct on (di.message_id) di.status as delivery_id\n" +
        "        from nc_delivery_info di\n" +
        "        where di.message_id = msg.id\n" +
        "        order by di.message_id, di.processed_on desc\n" +
        "    ) as latest_status,\n" +
        "    (\n" +
        "        SELECT json_agg(json_build_object(\n" +
        "            'id', ne.id,\n" +
        "            'name', ne.endpoint_name,\n" +
        "            'type', ne.endpoint_type\n" +
        "        ))\n" +
        "        FROM nc_message_2_endpoint_rel m2e\n" +
        "        JOIN public.nc_endpoint ne ON ne.id = m2e.endpoint_id\n" +
        "        WHERE m2e.message_id = msg.id\n" +
        "    ) AS endpoints,\n" +
        "    (\n" +
        "        SELECT json_agg(json_build_object(\n" +
        "            'id', e.id,\n" +
        "            'name', e.name,\n" +
        "            'description', e.description\n" +
        "        ))\n" +
        "        FROM nc_event e\n" +
        "        WHERE e.id = ANY (msg.previous_event_ids)\n" +
        "    ) AS events\n" +
        "from nc_message msg\n" +
        "where msg.time_created between :createdFrom and :createdTo\n" +
        "and :eventId::uuid is null or :eventId::uuid = any (msg.previous_event_ids)\n" +
        "offset :offset rows fetch next :pageSize rows only"
    )
    List<MessageDto> findAllMessages(
            @Param("createdFrom") Instant createdFrom,
            @Param("createdTo") Instant createdTo,
            @Param("eventId") UUID eventId,
            @Param("offset") long offset,
            @Param("pageSize") int pageSize
    );
    
    @Query(
        "select count(msg.id)\n" +
        "from nc_message msg\n" +
        "where msg.time_created between :createdFrom and :createdTo\n" +
        "and :eventId::uuid is null or :eventId::uuid = any(msg.previous_event_ids)"
    )
    long countAllMessages(
            @Param("createdFrom") Instant createdFrom,
            @Param("createdTo") Instant createdTo,
            @Param("eventId") UUID eventId
    );
    
}
