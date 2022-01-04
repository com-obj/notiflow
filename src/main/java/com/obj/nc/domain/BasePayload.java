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

package com.obj.nc.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.headers.HasHeader;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.headers.ProcessingInfo;
import com.obj.nc.domain.message.*;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({ 
	@Type(value = NotificationIntent.class, name = NotificationIntent.JSON_TYPE_IDENTIFIER),
	@Type(value = EmailMessage.class, name = EmailMessage.JSON_TYPE_IDENTIFIER),
	@Type(value = SmsMessage.class, name = SmsMessage.JSON_TYPE_IDENTIFIER),
	@Type(value = MailchimpMessage.class, name = MailchimpMessage.JSON_TYPE_IDENTIFIER),
	@Type(value = TemplatedMailchimpMessage.class, name = TemplatedMailchimpMessage.JSON_TYPE_IDENTIFIER),
	@Type(value = EmailMessageTemplated.class, name = EmailMessageTemplated.JSON_TYPE_IDENTIFIER),
	@Type(value = EmailWithTestModeDigest.class, name = EmailWithTestModeDigest.JSON_TYPE_IDENTIFIER),
	@Type(value = SmsMessageTemplated.class, name = SmsMessageTemplated.JSON_TYPE_IDENTIFIER),	
})
@ToString(callSuper = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public abstract class BasePayload<BODY_TYPE> extends BaseDynamicAttributesBean implements HasHeader, HasReceivingEndpoints, HasProcessingInfo, Persistable<UUID> {
	
	@Id
	@EqualsAndHashCode.Include
	private UUID id = UUID.randomUUID();
	@CreatedDate
	private Instant timeCreated;
	@Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
	protected Header header = new Header();
	
	@Column("payload_json")	
	protected BODY_TYPE body;
	
	//if body is part of message then receivingEndpoints.size() = 1
	private List<? extends ReceivingEndpoint> receivingEndpoints = new ArrayList<ReceivingEndpoint>();
	
	public BasePayload<BODY_TYPE> addReceivingEndpoints(ReceivingEndpoint ... r) {
		((List<ReceivingEndpoint>)this.receivingEndpoints).addAll(Arrays.asList(r));
		return this;
	}
		
	@JsonIgnore
	@Transient
	public void setReceivingEndpointsSL(List<ReceivingEndpoint> endpoints) {
		List<ReceivingEndpoint> typedEndpoints = Lists.newArrayList(
				   Iterables.filter(endpoints, ReceivingEndpoint.class));
		
		this.setReceivingEndpoints(typedEndpoints);
	}

	@Override
	@Transient
	public List<? extends ReceivingEndpoint> getReceivingEndpoints() {
		return receivingEndpoints;
	}
	
	@JsonIgnore
	@Transient
	public ProcessingInfo getProcessingInfo() {
		return getHeader().getProcessingInfo();
	}

	@Transient
	public abstract String getPayloadTypeName();
	
	@Override
	@JsonIgnore
	@Transient
	public boolean isNew() {
		return timeCreated == null;
	}

}
