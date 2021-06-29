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

import static com.obj.nc.flows.messageProcessing.MessageProcessingFlowConfig.MESSAGE_PROCESSING_FLOW_ID;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
public abstract class Message<BODY_TYPE extends MessageContent> extends BasePayload<BODY_TYPE> implements HasMessageId {
	
	//processing started, only if timeConsumed = null processing will be started
	private Instant timeConsumed;
	private String externalId;
	
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
	
	public void setFlowIdOrDefault(String flowId) {
		if (header.getFlowId() == null) {
			header.setFlowId(MESSAGE_PROCESSING_FLOW_ID);
		}
		
		if (flowId == null) {
			return;
		}
		
		header.setFlowId(flowId);
	}
	
	public void overrideExternalIdIfApplicable(String externalId) {
		if (externalId == null) {
			return;
		}
		
		this.externalId = externalId;
	}
	
}
