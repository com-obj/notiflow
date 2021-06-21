package com.obj.nc.domain.headers;

import java.util.List;
import java.util.UUID;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.NativeMessageHeaderAccessor;

/**
 * Inspired by AmqpMessageHeaderAccessor
 * @author ja
 *
 */
public class NotifCenterMessageHeaderAccessor extends NativeMessageHeaderAccessor {

	public static final String PREFIX = "#nc-";
	
	public static final String FLOW_ID = PREFIX + "flow-id";
	
	public static final String ID = PREFIX + "id";
	
	public static final String EVENT_IDS = PREFIX + "event-ids";
	
	public static final String PROCESSING_INFO = PREFIX + "processing-info";

	protected NotifCenterMessageHeaderAccessor(Message<?> message) {
		super(message);
	}

	// Static factory method
	public static NotifCenterMessageHeaderAccessor wrap(Message<?> message) {
		return new NotifCenterMessageHeaderAccessor(message);
	}

	@Override
	protected void verifyType(String headerName, Object headerValue) {
		super.verifyType(headerName, headerValue);
	}

	public String getFlowId() {
		return (String) getHeader(FLOW_ID);
	}
	
	public NotifCenterMessageHeaderAccessor setFlowId(String flowId) {
		setHeader(FLOW_ID, flowId);
		return this;
	}

	public UUID getId() {
		return (UUID) getHeader(ID);
	}
	
	public NotifCenterMessageHeaderAccessor setId(UUID flowId) {
		setHeader(ID, flowId);
		return this;
	}

	public List<UUID> getEventIds() {
		return (List<UUID>) getHeader(EVENT_IDS);
	}
	
	public NotifCenterMessageHeaderAccessor setEventIds(List<UUID> eventIds) {
		setHeader(EVENT_IDS, eventIds);
		return this;
	}
	
	public void addEventId(UUID eventId) {
		getEventIds().add(eventId);
	}

	public ProcessingInfo getProcessingInfo() {
		return (ProcessingInfo) getHeader(PROCESSING_INFO);
	}
	
	public NotifCenterMessageHeaderAccessor setProcessingInfo(ProcessingInfo processingInfo) {
		setHeader(PROCESSING_INFO, processingInfo);
		return this;
	}

}
