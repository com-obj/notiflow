package com.obj.nc.domain.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.IsNotification;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.endpoints.RecievingEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
public abstract class Message<BODY_TYPE extends MessageContent> extends BasePayload<BODY_TYPE> implements IsNotification {
		
	@JsonIgnore
	public abstract Class<? extends RecievingEndpoint> getRecievingEndpointType();
	
	public MessagePersistantState toPersistantState() {
		MessagePersistantState persistantState = new MessagePersistantState();
		persistantState.setBody(getBody());
		persistantState.setHeader(getHeader());
		persistantState.setId(getId());
		persistantState.setMessageClass(getClass().getName());
		persistantState.setTimeCreated(getTimeCreated());
		persistantState.setEndpointIds(getRecievingEndpoints().stream().map(RecievingEndpoint::getId).toArray(UUID[]::new));
		return persistantState;	 
	}
	
}
