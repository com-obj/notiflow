package com.obj.nc.domain.endpoints;

import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.obj.nc.domain.deliveryOptions.DeliveryOptions;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({ 
	@Type(value = EmailEndpoint.class, name = EmailEndpoint.JSON_TYPE_IDENTIFIER), 
	@Type(value = MailchimpEndpoint.class, name = MailchimpEndpoint.JSON_TYPE_IDENTIFIER),
	@Type(value = SmsEndpoint.class, name = SmsEndpoint.JSON_TYPE_IDENTIFIER)
})
@Data
@NoArgsConstructor
@Table("nc_endpoint")
public abstract class RecievingEndpoint {
	
	/**
	 * Kazdy Endpoint (Email, SMS, PUSH) ma nastavene options. Kedy na neho mozes posielat, ci agregovat. 
	 * Je mozne, ze niektore setting by mali byt aj pre Recipienta tj. take ktore su platne nezavisle od kanala. Zatial ale sa budem tvarit, ze ak aj take budu
	 * prekopiruju(zmerguju) sa k danemu enpointu
	 */
	private DeliveryOptions deliveryOptions;
	private Recipient recipient;
	
	public abstract String getEndpointId();
	
	public abstract void setEndpointId(String endpointId);
	
	@JsonIgnore
	public abstract String getEndpointType();

}
