package com.obj.nc.domain.endpoints;

import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class Person extends Recipient{
	
	public static final String JSON_TYPE_IDENTIFIER = "PERSON";

	@NotNull
	private String name;
	
	public Person(String name) {
		this.name = name;
	}

	@Override
	public List<Person> findFinalRecipientsAsPersons() {
		return Arrays.asList(this);
	}

}
