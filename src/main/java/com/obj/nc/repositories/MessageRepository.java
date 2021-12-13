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

import com.obj.nc.domain.dto.MessageTableViewDto;
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
            value = "select " +
                    "	msg.*, array(select endpoint_id from nc_message_2_endpoint_rel m2e where m2e.message_id = msg.id) as endpoint_ids " +
                    "from " +
                    "	nc_message msg " +
                    "where " +
                    "	msg.time_created between (:createdFrom) and (:createdTo) " +
                    "and " +
                    "	(:eventId)::uuid is null or (:eventId)::uuid = any ( msg.previous_event_ids ) " +
                    "offset :offset rows fetch next :pageSize rows only",
            rowMapperClass = MessageTableViewDto.MessageTableViewDtoRowMapper.class)
    List<MessageTableViewDto> findAllMessages(@Param("createdFrom") Instant createdFrom,
                                              @Param("createdTo") Instant createdTo,
                                              @Param("eventId") UUID eventId,
                                              @Param("offset") long offset,
                                              @Param("pageSize") int pageSize);
    
    @Query(
            value = "select " +
                    "	count(msg.id) " +
                    "from " +
                    "	nc_message msg " +
                    "where " +
                    "	msg.time_created between (:createdFrom) and (:createdTo) " +
                    "and " +
                    "	(:eventId)::uuid is null or (:eventId)::uuid = any ( msg.previous_event_ids ) ")
    long countAllMessages(@Param("createdFrom") Instant createdFrom,
                          @Param("createdTo") Instant createdTo,
                          @Param("eventId") UUID eventId);
    
}
