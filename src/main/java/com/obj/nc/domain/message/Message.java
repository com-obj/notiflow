package com.obj.nc.domain.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.*;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.endpoints.RecievingEndpoint;

import com.obj.nc.domain.refIntegrity.Reference;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.MessageRepository;
import com.obj.nc.repositories.NotificationIntentRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
public abstract class Message<BODY_TYPE extends MessageContent> extends BasePayload<BODY_TYPE> implements IsNotification, HasEventIds, HasIntentIds, HasMessageIds {
	
	@NotNull
	@EqualsAndHashCode.Include
	@Transient
	@Reference(GenericEventRepository.class)
	private List<UUID> eventIds = new ArrayList<>();
	
	@NotNull
	@EqualsAndHashCode.Include
	@Transient
	@Reference(NotificationIntentRepository.class)
	private List<UUID> intentIds = new ArrayList<>();
	
	@NotNull
	@EqualsAndHashCode.Include
	@Transient
	@Reference(MessageRepository.class)
	private List<UUID> messageIds = new ArrayList<>();
		
	@JsonIgnore
	public abstract Class<? extends RecievingEndpoint> getRecievingEndpointType();
	
	public MessagePersistantState toPersistantState() {
		MessagePersistantState persistantState = new MessagePersistantState();
		persistantState.setBody(getBody());
		persistantState.setHeader(getHeader());
		persistantState.setId(getId());
		persistantState.setEventIds(eventIds.toArray(new UUID[0]));
		persistantState.setIntentIds(intentIds.toArray(new UUID[0]));
		persistantState.setMessageIds(messageIds.toArray(new UUID[0]));
		persistantState.setMessageClass(getClass().getName());
		persistantState.setTimeCreated(getTimeCreated());
		persistantState.setEndpointIds(getRecievingEndpoints().stream().map(RecievingEndpoint::getId).toArray(UUID[]::new));
		return persistantState;	 
	}
	
	public void addEventId(UUID eventId) {
		eventIds.add(eventId);
	}
	
	public void addIntentId(UUID intentId) {
		intentIds.add(intentId);
	}
	
	public void addMessageId(UUID messageId) {
		messageIds.add(messageId);
	}
	
	@JsonIgnore
	@Column("event_ids")
	public void setEventIdsAsArray(UUID[] eventIds) {
		setEventIds(Arrays.asList(eventIds));
	}
	
	@Column("event_ids")
	public UUID[] getEventIdsAsArray() {
		return eventIds.toArray(new UUID[0]);
	}
	
	@JsonIgnore
	@Column("intent_ids")
	public void setIntentIdsAsArray(UUID[] intentIds) {
		setIntentIds(Arrays.asList(intentIds));
	}
	
	@Column("intent_ids")
	public UUID[] getIntentIdsAsArray() {
		return intentIds.toArray(new UUID[0]);
	}
	
	@JsonIgnore
	@Column("message_ids")
	public void setMessageIdsAsArray(UUID[] messageIds) {
		setIntentIds(Arrays.asList(messageIds));
	}
	
	@Column("message_ids")
	public UUID[] getMessageIdsAsArray() {
		return messageIds.toArray(new UUID[0]);
	}
	
}
