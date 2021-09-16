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

package com.obj.nc.domain.message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.HasPreviousIntentIds;
import com.obj.nc.domain.HasPreviousMessageIds;
import com.obj.nc.domain.IsNotification;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.notifIntent.NotificationIntent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
public abstract class Message<BODY_TYPE extends MessageContent> extends BasePayload<BODY_TYPE> implements IsNotification, HasPreviousIntentIds, HasPreviousMessageIds {
	
	@NotNull
	@EqualsAndHashCode.Include
	private List<UUID> previousEventIds = new ArrayList<>();
	
	@NotNull
	@EqualsAndHashCode.Include
	private List<UUID> previousIntentIds = new ArrayList<>();
	
	@NotNull
	@EqualsAndHashCode.Include
	private List<UUID> previousMessageIds = new ArrayList<>();
	
	@SneakyThrows
	public static <T extends Message<?>> T newTypedMessageFrom(Class<T> messageType,
															   Message<?> ... messages) {
		if (messages.length == 0) {
			return null;
		}
		
		T newMessage = messageType.newInstance();
		
		if (messages.length == 1) {
			newMessage.getHeader().setFlowId(messages[0].getHeader().getFlowId());
		}
		
		for (Message<?> message : messages) {
			// TODO: consider using Set instead of list
			message.getPreviousEventIds()
					.stream()
					.filter(eventId -> !newMessage.getPreviousEventIds().contains(eventId))
					.forEach(newMessage::addPreviousEventId);
			
			message.getPreviousIntentIds()
					.stream()
					.filter(intentId -> !newMessage.getPreviousIntentIds().contains(intentId))
					.forEach(newMessage::addPreviousIntentId);
			
			newMessage.addPreviousMessageId(message.getId());
		}
		
		return newMessage;
	}
	
	@SneakyThrows
	public static <T extends Message<?>> T newTypedMessageFrom(Class<T> messageType,
															   NotificationIntent intent) {
		T newMessage = messageType.newInstance();
		
		newMessage.getHeader().setFlowId(intent.getHeader().getFlowId());
		
		intent.getPreviousEventIds().forEach(newMessage::addPreviousEventId);
		newMessage.addPreviousIntentId(intent.getId());
		
		return newMessage;
	}
		
	@JsonIgnore
	public abstract Class<? extends ReceivingEndpoint> getReceivingEndpointType();
	
	public MessagePersistentState toPersistentState() {
		MessagePersistentState persistentState = new MessagePersistentState();
		persistentState.setBody(getBody());
		persistentState.setHeader(getHeader());
		persistentState.setId(getId());
		persistentState.setPreviousEventIds(previousEventIds.toArray(new UUID[0]));
		persistentState.setPreviousIntentIds(previousIntentIds.toArray(new UUID[0]));
		persistentState.setPreviousMessageIds(previousMessageIds.toArray(new UUID[0]));
		persistentState.setMessageClass(getClass().getName());
		persistentState.setTimeCreated(getTimeCreated());
		persistentState.setEndpointIds(getReceivingEndpoints().stream().map(ReceivingEndpoint::getId).toArray(UUID[]::new));
		return persistentState;	 
	}
	
	public void addPreviousEventId(UUID eventId) {
		previousEventIds.add(eventId);
	}
	
	public void addPreviousIntentId(UUID previousIntentId) {
		previousIntentIds.add(previousIntentId);
	}
	
	public void addPreviousMessageId(UUID previousMessageId) {
		previousMessageIds.add(previousMessageId);
	}
	
}
