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

import com.google.common.collect.ObjectArrays;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;

@Data
@EqualsAndHashCode(callSuper=false, of = "email")
@RequiredArgsConstructor
@NoArgsConstructor
@Builder
public class EmailEndpoint extends ReceivingEndpoint {
	
	public static final String JSON_TYPE_IDENTIFIER = "EMAIL";

	@NonNull
	private String email;
	
	public static EmailEndpoint createForPerson(Person person, String emailAddress) {
		EmailEndpoint r = EmailEndpoint.builder()
				.email(emailAddress)
				.build();
		
		r.setRecipient(person);
		return r;
	}
	
	public static EmailEndpoint createForGroup(Group group, String emailAddress) {
		EmailEndpoint r = EmailEndpoint.builder()
				.email(emailAddress)
				.build();
		
		r.setRecipient(group);
		return r;
	}

	@Override
	public String getEndpointId() {
		return email;
	}
	
	@Override
	public void setEndpointId(String endpointId) {
		this.email = endpointId;
	}

	@Override
	public String getEndpointType() {
		return JSON_TYPE_IDENTIFIER;
	}

	public static FieldDescriptor[] fieldDesc = ObjectArrays.concat(
        ReceivingEndpoint.fieldDesc,
        new FieldDescriptor[] {
			PayloadDocumentation.fieldWithPath("email").description("Email address"),        
        },
        FieldDescriptor.class
    );

	
	
}
