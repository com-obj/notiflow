package com.obj.nc.domain.endpoints;

import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ 
	@Type(value = EmailEndpoint.class, name = EmailEndpoint.JSON_TYPE_IDENTIFIER), 
	@Type(value = MailChimpEndpoint.class, name = MailChimpEndpoint.JSON_TYPE_IDENTIFIER),
	@Type(value = SmsEndpoint.class, name = SmsEndpoint.JSON_TYPE_IDENTIFIER)
})
@Data
@NoArgsConstructor
@Table("nc_endpoint")
public abstract class RecievingEndpoint {
	
	private DeliveryOptions deliveryOptions;
	private Recipient recipient;
	
	@JsonIgnore
	public abstract String getEndpointId();
	
	@JsonIgnore
	public abstract String getEndpointTypeName();

}
