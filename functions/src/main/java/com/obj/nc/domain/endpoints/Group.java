package com.obj.nc.domain.endpoints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class Group implements Recipient{
	
	@NotNull
	private final String name;
	
	private List<Recipient> members = new ArrayList<>();
	
	private boolean wasRsolved = false;
	
	public static Group createWithMembers(String name, Recipient ... members ) {
		Group group = new Group(name);
		group.members.addAll(Arrays.asList(members));
		group.wasRsolved = true;
		return group;
	}

	@Override
	public List<Person> getFinalRecipientsAsPersons() {
		return members.stream().flatMap(rec -> rec.getFinalRecipientsAsPersons().stream()).collect(Collectors.toList());
	}

}
