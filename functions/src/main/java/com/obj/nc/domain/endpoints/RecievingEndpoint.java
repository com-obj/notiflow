package com.obj.nc.domain.endpoints;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ 
	@Type(value = EmailEndpoint.class, name = EmailEndpoint.JSON_TYPE_IDENTIFIER), 
	@Type(value = EmailEndpoint.class, name = MailChimpEndpoint.JSON_TYPE_IDENTIFIER)
})
@Data
@NoArgsConstructor
public abstract class RecievingEndpoint {
	
	private DeliveryOptions deliveryOptions;
	private Recipient recipient;
	
	public abstract String getEndpointId();
	
	@JsonIgnore
	public abstract String getEndpointTypeName();

}
