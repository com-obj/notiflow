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

package com.obj.nc.domain.endpoints;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.obj.nc.domain.deliveryOptions.DeliveryOptions;

import com.obj.nc.domain.endpoints.push.DirectPushEndpoint;
import com.obj.nc.domain.endpoints.push.TopicPushEndpoint;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({ 
	@Type(value = EmailEndpoint.class, name = EmailEndpoint.JSON_TYPE_IDENTIFIER), 
	@Type(value = MailchimpEndpoint.class, name = MailchimpEndpoint.JSON_TYPE_IDENTIFIER),
	@Type(value = SmsEndpoint.class, name = SmsEndpoint.JSON_TYPE_IDENTIFIER),
	@Type(value = DirectPushEndpoint.class, name = DirectPushEndpoint.JSON_TYPE_IDENTIFIER),
	@Type(value = TopicPushEndpoint.class, name = TopicPushEndpoint.JSON_TYPE_IDENTIFIER)
})
@Data
@NoArgsConstructor
@Table("nc_endpoint")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public abstract class ReceivingEndpoint implements Persistable<UUID> {
	
	/**
	 * Kazdy Endpoint (Email, SMS, PUSH) ma nastavene options. Kedy na neho mozes posielat, ci agregovat. 
	 * Je mozne, ze niektore setting by mali byt aj pre Recipienta tj. take ktore su platne nezavisle od kanala. Zatial ale sa budem tvarit, ze ak aj take budu
	 * prekopiruju(zmerguju) sa k danemu enpointu
	 */
	@Id
	@EqualsAndHashCode.Include
	private UUID id = UUID.randomUUID();
	private DeliveryOptions deliveryOptions;
	private Recipient recipient;
	@CreatedDate
	private Instant timeCreated;
	
	public abstract String getEndpointId();
	
	public abstract void setEndpointId(String endpointId);
	
	@JsonIgnore
	public abstract String getEndpointType();
	
	@Override
	@JsonIgnore
	@Transient
	public boolean isNew() {
		return timeCreated == null;
	}

	public static FieldDescriptor[] fieldDesc = new FieldDescriptor[] {
		PayloadDocumentation.fieldWithPath("id").description("Internal ID"),
		PayloadDocumentation.fieldWithPath("deliveryOptions").description("TODO"),
		PayloadDocumentation.fieldWithPath("recipient").description("Information about real recipient to whom this endpoint belongs to"),
		PayloadDocumentation.fieldWithPath("endpointId").description("Non technical ID of this endpoint (Email address, Phone number, .."),
		PayloadDocumentation.fieldWithPath("timeCreated").description("Timestamp of record creation"),		
		PayloadDocumentation.fieldWithPath("@type").description("Type discriminator of the endpoint type. Will be replaced by @class attribute in the future"),
	};

}
