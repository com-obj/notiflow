package com.obj.nc.domain.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.HasMessageId;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.endpoints.RecievingEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
public abstract class Message<BODY_TYPE extends MessageContent> extends BasePayload<BODY_TYPE> implements HasMessageId {
	
	//processing started, only if timeConsumed = null processing will be started
	private Instant timeConsumed;
	
	@JsonIgnore
	public abstract Class<? extends RecievingEndpoint> getRecievingEndpointType();
	
	@JsonIgnore
	@Override
	public UUID getMessageId() {
		return getId();
	}
		
	public MessagePersistantState toPersistantState() {
		MessagePersistantState persistantState = new MessagePersistantState();
		persistantState.setBody(getBody());
		persistantState.setHeader(getHeader());
		persistantState.setId(getId());
		persistantState.setMessageClass(getClass().getName());
		persistantState.setTimeCreated(getTimeCreated());
		persistantState.setTimeConsumed(getTimeConsumed());
		persistantState.setEndpointIds(getRecievingEndpoints().stream().map(RecievingEndpoint::getId).toArray(UUID[]::new));
		return persistantState;	 
	}
	
}
