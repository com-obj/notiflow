package com.obj.nc.domain.endpoints;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ 
	@Type(value = EmailEndpoint.class, name = "EMAIL"), 
	@Type(value = EmailEndpoint.class, name = "MAIL_CHIMP")
})
@Data
@NoArgsConstructor
public abstract class RecievingEndpoint {
	
	private DeliveryOptions deliveryOptions;

}
