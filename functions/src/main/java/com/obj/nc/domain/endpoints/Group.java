package com.obj.nc.domain.endpoints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class Group extends Recipient{
	
	public static final String JSON_TYPE_IDENTIFIER = "GROUP";
	
	@NotNull
	private String name;
	
	private List<Recipient> members = new ArrayList<>();
	
	private boolean wasRsolved = false;
	
	public Group(String name) {
		this.name = name;
	}
	
	public static Group createWithMembers(String name, Recipient ... members ) {
		Group group = new Group(name);
		group.members.addAll(Arrays.asList(members));
		group.wasRsolved = true;
		return group;
	}

	@Override
	public List<Person> findFinalRecipientsAsPersons() {
		return members.stream().flatMap(rec -> rec.findFinalRecipientsAsPersons().stream()).collect(Collectors.toList());
	}

}
