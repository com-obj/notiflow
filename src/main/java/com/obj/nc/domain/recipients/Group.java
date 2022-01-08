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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.obj.nc.domain.endpoints.ReceivingEndpoint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@EqualsAndHashCode(callSuper=false, onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group extends Recipient{
	
	public static final String JSON_TYPE_IDENTIFIER = "GROUP";

	@NotNull
	@Builder.Default
	@EqualsAndHashCode.Include
	private UUID id = UUID.randomUUID();
	
	@NotNull
	private String name;

	@Singular
	private List<ReceivingEndpoint> receivingEndpoints;

	@Singular
	private List<Recipient> members;

	
	public static Group createWithMembers(String name, Recipient ... members ) {
		Group group = Group.builder()
				.name(name)
				.members(Arrays.asList(members))
				.build();
		
		return group;
	}

	@Override
	public List<Person> findFinalRecipientsAsPersons() {
		return members.stream().flatMap(rec -> rec.findFinalRecipientsAsPersons().stream()).collect(Collectors.toList());
	}

}
