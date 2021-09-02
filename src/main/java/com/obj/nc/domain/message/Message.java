package com.obj.nc.domain.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.*;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.endpoints.RecievingEndpoint;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.*;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
public abstract class Message<BODY_TYPE extends MessageContent> extends BasePayload<BODY_TYPE> implements IsNotification, HasPreviousEventIds, HasPreviousIntentIds, HasPreviousMessageIds {
	
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
	public abstract Class<? extends RecievingEndpoint> getRecievingEndpointType();
	
	public MessagePersistantState toPersistantState() {
		MessagePersistantState persistantState = new MessagePersistantState();
		persistantState.setBody(getBody());
		persistantState.setHeader(getHeader());
		persistantState.setId(getId());
		persistantState.setPreviousEventIds(previousEventIds.toArray(new UUID[0]));
		persistantState.setPreviousIntentIds(previousIntentIds.toArray(new UUID[0]));
		persistantState.setPreviousMessageIds(previousMessageIds.toArray(new UUID[0]));
		persistantState.setMessageClass(getClass().getName());
		persistantState.setTimeCreated(getTimeCreated());
		persistantState.setEndpointIds(getRecievingEndpoints().stream().map(RecievingEndpoint::getId).toArray(UUID[]::new));
		return persistantState;	 
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
