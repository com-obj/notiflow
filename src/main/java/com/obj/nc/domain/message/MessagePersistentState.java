/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.domain.message;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.Get;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.refIntegrity.Reference;
import com.obj.nc.repositories.EndpointsRepository;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.MessageRepository;
import com.obj.nc.repositories.NotificationIntentRepository;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;

@Data
@Table("nc_message")
public class MessagePersistentState implements Persistable<UUID> {
	

	@Id
	@EqualsAndHashCode.Include
	private UUID id;
	@CreatedDate
	private Instant timeCreated;
	@Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
	private Header header;
	
	@Column("content_json")
	private MessageContent body;
	
	private String messageClass;
	
	@NotNull
	@Reference(EndpointsRepository.class)
	private UUID[] endpointIds;
	
	@NotNull
	@Reference(GenericEventRepository.class)
	private UUID[] previousEventIds;
	
	@NotNull
	@Reference(NotificationIntentRepository.class)
	private UUID[] previousIntentIds;
	
	@NotNull
	@Reference(MessageRepository.class)
	private UUID[] previousMessageIds;
	
	@JsonIgnore
	@Transient
	private List<ReceivingEndpoint> receivingEndpoints;
	
	@Override
	@JsonIgnore
	@Transient
	public boolean isNew() {
		return timeCreated == null;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@SneakyThrows
	public <T extends Message> T toMessage() {
		T msg = (T)Class.forName(messageClass).newInstance();
		msg.setBody(getBody());
		msg.setHeader(getHeader());
		msg.setId(getId());
		msg.setTimeCreated(getTimeCreated());
		
		List<ReceivingEndpoint> endpoints = findReceivingEndpoints();
		msg.setReceivingEndpoints(endpoints);
		
		msg.setPreviousEventIds(Arrays.asList(previousEventIds));
		msg.setPreviousIntentIds(Arrays.asList(previousIntentIds));
		msg.setPreviousMessageIds(Arrays.asList(previousMessageIds));
		
		return msg;
	}
	
	public List<ReceivingEndpoint> findReceivingEndpoints() {
		if (receivingEndpoints == null) {
			receivingEndpoints = Get.getEndpointsRepo().findByIds(getEndpointIds());
		}
		return receivingEndpoints;
	}
	
}
