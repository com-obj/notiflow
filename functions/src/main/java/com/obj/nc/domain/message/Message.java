package com.obj.nc.domain.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.HasMessageId;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.endpoints.RecievingEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
public abstract class Message<BODY_TYPE extends MessageContent> extends BasePayload<BODY_TYPE> implements HasMessageId {
		
	@JsonIgnore
	public abstract Class<? extends RecievingEndpoint> getRecievingEndpointType();
	
	@JsonIgnore
	@Override
	public UUID getMessageId() {
		return getId();
	}
		
	public MessagePersistantState toPersistantState() {
		return MessagePersistantState.builder()
			.body(getBody())
			.header(getHeader())
			.id(getId())
			.messageClass(getClass().getName())
			.timeCreated(getTimeCreated())
			.endpointIds(getRecievingEndpoints().stream().map(RecievingEndpoint::getEndpointId).collect(Collectors.toList()))
			.build();		 
	}
	
}
