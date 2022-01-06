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

package com.obj.nc.domain.notifIntent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.api.client.util.Lists;
import com.google.common.collect.Iterables;
import com.obj.nc.domain.BaseDynamicAttributesBean;
import com.obj.nc.domain.HasPreviousEventIds;
import com.obj.nc.domain.HasProcessingInfo;
import com.obj.nc.domain.HasReceivingEndpoints;
import com.obj.nc.domain.IsNotification;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.headers.HasHeader;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.headers.ProcessingInfo;
import com.obj.nc.domain.notifIntent.content.IntentContent;
import com.obj.nc.domain.refIntegrity.Reference;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.NotificationIntentRepository;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(callSuper = false)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
/**
 * This class represents Intent to deliver *some* kind of information at *some* point in time to recipient. Use this class in case
 * that you cannot tell the details about what/when/how are stored in delivery settings of that recipient. 
 * Example:
 * 		Recipient has two endpoints: Email, SMS
 * 		His settings are that he wants to be notified using SMS in working hours and by Email outside of working hours.
 * 		In addition his settings are that he want to receive at most 3 email in a week
 * 
 * Create notification intent and let Notiflow to decide if 
 * 		SMS should be send immediately 
 * 			OR
 * 		Email should be send immediately
 * 			OR
 * 		Email should be send later in form of email aggregate
 * @author ja
 *
 * @param <BODY_TYPE>
 */
public class NotificationIntent extends BaseDynamicAttributesBean implements HasHeader, HasReceivingEndpoints, HasProcessingInfo, IsNotification, HasPreviousEventIds {
	
	@EqualsAndHashCode.Include
	private UUID id = UUID.randomUUID();

	private Instant timeCreated;

	protected Header header = new Header();
	
	protected IntentContent body;
	
	//TODO: purely technical staf, should be moved to header
	@Transient
	@Reference(GenericEventRepository.class)
	private List<UUID> previousEventIds = new ArrayList<>();
	
	//TODO: purely technical staf, should be moved to header
	//Can Intent have parent Intent? What is that good for?
	@Transient
	@Reference(NotificationIntentRepository.class)
	private List<UUID> previousIntentIds = new ArrayList<>();

	private List<? extends ReceivingEndpoint> receivingEndpoints = new ArrayList<ReceivingEndpoint>();

	public static NotificationIntent createWithStaticContent(String subject, String body, ReceivingEndpoint ... endpoints) {
		NotificationIntent intent = new NotificationIntent();	
		intent.setBody(IntentContent.createStaticContent(subject,body));
		intent.addReceivingEndpoints(endpoints);
		return intent;
	}

	public NotificationIntentPersistentState toPersistentState() {
		NotificationIntentPersistentState persistentState = new NotificationIntentPersistentState();
		persistentState.setBody(getBody());
		persistentState.setHeader(getHeader());
		persistentState.setId(getId());
		persistentState.setPreviousEventIds(previousEventIds.toArray(new UUID[0]));
		persistentState.setPreviousIntentIds(previousIntentIds.toArray(new UUID[0]));
		persistentState.setTimeCreated(getTimeCreated());
		return persistentState;	 
	}

	public NotificationIntent addReceivingEndpoints(ReceivingEndpoint ... r) {
		((List<ReceivingEndpoint>)this.receivingEndpoints).addAll(Arrays.asList(r));
		return this;
	}
		
	@JsonIgnore
	public void setReceivingEndpointsSL(List<ReceivingEndpoint> endpoints) {
		List<ReceivingEndpoint> typedEndpoints = Lists.newArrayList(
					Iterables.filter(endpoints, ReceivingEndpoint.class));
		
		this.setReceivingEndpoints(typedEndpoints);
	}

	@Override
	public List<? extends ReceivingEndpoint> getReceivingEndpoints() {
		return receivingEndpoints;
	}
	
	@Override
	public void addPreviousEventId(UUID eventId) {
		previousEventIds.add(eventId);
	}
	
	public void addPreviousIntentId(UUID intentId) {
		previousIntentIds.add(intentId);
	}

	@JsonIgnore
	public ProcessingInfo getProcessingInfo() {
		return getHeader().getProcessingInfo();
	}
	
	@JsonIgnore
	public boolean isNew() {
		return timeCreated == null;
	}
		
}
