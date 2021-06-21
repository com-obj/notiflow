package com.obj.nc.domain.endpoints;

import java.util.Arrays;
import java.util.List;

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
public class Person extends Recipient{
	
	public static final String JSON_TYPE_IDENTIFIER = "PERSON";
	
	private DeliveryOptions deliveryOptions;

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
