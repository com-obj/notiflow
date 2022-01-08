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

package com.obj.nc.domain.recipients;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.obj.nc.domain.deliveryOptions.EndpointDeliveryOptions;
import com.obj.nc.domain.deliveryOptions.RecipientDeliveryOptions;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ 
	@Type(value = Person.class, name = Person.JSON_TYPE_IDENTIFIER), 
	@Type(value = Group.class, name = Group.JSON_TYPE_IDENTIFIER)
})
@Data
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class Recipient {
	
	@ToString.Include
	public abstract String getName();

	@ToString.Include
	@EqualsAndHashCode.Include
	public abstract UUID getId();
	
	public abstract List<Person> findFinalRecipientsAsPersons();	

	//this should be some more notiflow unspecific term,.. kind of dto, only type and value like email,aaa@aaa.com
	public abstract List<ReceivingEndpoint> getReceivingEndpoints();


}
