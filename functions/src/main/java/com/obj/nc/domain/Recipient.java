package com.obj.nc.domain;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ 
	@Type(value = EmailRecipient.class, name = "EMAIL"), 
	@Type(value = Recipient.class, name = "RECIPIENT") 
})
@Data
@NoArgsConstructor
public class Recipient {

	@NotNull
	private String name;
	
	private DeliveryOptions deliveryOptions;

}
