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

package com.obj.nc.domain.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = false)
public class EmailMessage extends Message<EmailContent/*, EmailEndpoint*/> {

	public static final String JSON_TYPE_IDENTIFIER = "EMAIL_MESSAGE";
	
	public EmailMessage() {
		setBody(new EmailContent());
	}
	
	@Override
	public List<EmailEndpoint> getReceivingEndpoints() {
		return (List<EmailEndpoint>) super.getReceivingEndpoints();
	}

	@Override
	@JsonIgnore
	public String getPayloadTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}
	
	//TODO: refactor as class parameter
	@JsonIgnore
	public Class<? extends ReceivingEndpoint> getReceivingEndpointType() {
		return EmailEndpoint.class;
	}
	
}
