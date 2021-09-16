/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.domain.endpoints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.obj.nc.domain.deliveryOptions.DeliveryOptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group extends Recipient{
	
	public static final String JSON_TYPE_IDENTIFIER = "GROUP";
	
	@NotNull
	private String name;
	
	@Builder.Default
	private List<Recipient> members = new ArrayList<>();
	
	@Builder.Default
	private boolean wasResolved = false;
	
	private DeliveryOptions deliveryOptions; 
	
	public Group(String name) {
		this.name = name;
	}
	
	public static Group createWithMembers(String name, Recipient ... members ) {
		Group group = Group.builder()
				.name(name)
				.members(Arrays.asList(members))
				.wasResolved(true)
				.build();
		
		return group;
	}

	@Override
	public List<Person> findFinalRecipientsAsPersons() {
		return members.stream().flatMap(rec -> rec.findFinalRecipientsAsPersons().stream()).collect(Collectors.toList());
	}

}
