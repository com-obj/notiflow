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

package com.obj.nc.domain.headers;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.NativeMessageHeaderAccessor;

import java.util.List;
import java.util.UUID;

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
