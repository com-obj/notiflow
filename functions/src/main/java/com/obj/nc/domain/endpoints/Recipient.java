package com.obj.nc.domain.endpoints;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ 
	@Type(value = Person.class, name = Person.JSON_TYPE_IDENTIFIER), 
	@Type(value = Group.class, name = Group.JSON_TYPE_IDENTIFIER)
})
@Data
@NoArgsConstructor
public abstract class Recipient {
	
	public abstract String getName();
	
	public abstract List<Person> findFinalRecipientsAsPersons();


}
