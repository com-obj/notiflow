package com.obj.nc.domain.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.*;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.endpoints.RecievingEndpoint;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.domain.refIntegrity.Reference;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.MessageRepository;
import com.obj.nc.repositories.NotificationIntentRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.NotNull;
import java.util.*;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
public abstract class Message<BODY_TYPE extends MessageContent> extends BasePayload<BODY_TYPE> implements IsNotification, HasEventIds, HasPreviousIntentIds, HasPreviousMessageIds {
	
	@NotNull
	@EqualsAndHashCode.Include
	@Transient
	@Reference(GenericEventRepository.class)
	private List<UUID> eventIds = new ArrayList<>();
	
	@NotNull
	@EqualsAndHashCode.Include
	@Transient
	@Reference(NotificationIntentRepository.class)
	private List<UUID> previousIntentIds = new ArrayList<>();
	
	@NotNull
	@EqualsAndHashCode.Include
	@Transient
	@Reference(MessageRepository.class)
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
			message.getEventIds().forEach(newMessage::addEventId);
			message.getPreviousIntentIds().forEach(newMessage::addPreviousIntentId);
			newMessage.addPreviousMessageId(message.getId());
		}
		
		return newMessage;
	}
	
	@SneakyThrows
	public static <T extends Message<?>> T newTypedMessageFrom(Class<T> messageType,
															   NotificationIntent intent) {
		T newMessage = messageType.newInstance();
		
		newMessage.getHeader().setFlowId(intent.getHeader().getFlowId());
		
		intent.getEventIds().forEach(newMessage::addEventId);
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
		persistantState.setEventIds(eventIds.toArray(new UUID[0]));
		persistantState.setPreviousIntentIds(previousIntentIds.toArray(new UUID[0]));
		persistantState.setPreviousMessageIds(previousMessageIds.toArray(new UUID[0]));
		persistantState.setMessageClass(getClass().getName());
		persistantState.setTimeCreated(getTimeCreated());
		persistantState.setEndpointIds(getRecievingEndpoints().stream().map(RecievingEndpoint::getId).toArray(UUID[]::new));
		return persistantState;	 
	}
	
	public void addEventId(UUID eventId) {
		eventIds.add(eventId);
	}
	
	public void addPreviousIntentId(UUID previousIntentId) {
		previousIntentIds.add(previousIntentId);
	}
	
	public void addPreviousMessageId(UUID previousMessageId) {
		previousMessageIds.add(previousMessageId);
	}
	
	@JsonIgnore
	@Column("event_ids")
	public void setEventIdsAsArray(UUID[] eventIds) {
		setEventIds(Arrays.asList(eventIds));
	}
	
	@JsonIgnore
	@Column("previous_intent_ids")
	public void setPreviousIntentIdsAsArray(UUID[] intentIds) {
		setPreviousIntentIds(Arrays.asList(intentIds));
	}
	
	@JsonIgnore
	@Column("previous_message_ids")
	public void setPreviousMessageIdsAsArray(UUID[] messageIds) {
		setPreviousIntentIds(Arrays.asList(messageIds));
	}
	
	@Column("event_ids")
	public UUID[] getEventIdsAsArray() {
		return eventIds.toArray(new UUID[0]);
	}
	
	@Column("previous_intent_ids")
	public UUID[] getPreviousIntentIdsAsArray() {
		return previousIntentIds.toArray(new UUID[0]);
	}
	
	@Column("previous_message_ids")
	public UUID[] getPreviousMessageIdsAsArray() {
		return previousMessageIds.toArray(new UUID[0]);
	}
	
}
