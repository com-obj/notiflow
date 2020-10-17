package com.obj.nc.domain.endpoints;

import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class Person implements Recipient{
	
	@NotNull
	private final String name;

	@Override
	public List<Person> getFinalRecipientsAsPersons() {
		return Arrays.asList(this);
	}

}
