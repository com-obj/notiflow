package com.obj.nc.domain.endpoints;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.obj.nc.domain.deliveryOptions.DeliveryOptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public abstract class RecievingEndpoint implements Persistable<UUID> {
	
	/**
	 * Kazdy Endpoint (Email, SMS, PUSH) ma nastavene options. Kedy na neho mozes posielat, ci agregovat. 
	 * Je mozne, ze niektore setting by mali byt aj pre Recipienta tj. take ktore su platne nezavisle od kanala. Zatial ale sa budem tvarit, ze ak aj take budu
	 * prekopiruju(zmerguju) sa k danemu enpointu
	 */
	@Id
	@EqualsAndHashCode.Include
	private UUID id = UUID.randomUUID();
	private DeliveryOptions deliveryOptions;
	private Recipient recipient;
	@CreatedDate
	private Instant timeCreated;
	
	public abstract String getEndpointId();
	
	public abstract void setEndpointId(String endpointId);
	
	@JsonIgnore
	public abstract String getEndpointType();
	
	@Override
	@JsonIgnore
	@Transient
	public boolean isNew() {
		return timeCreated == null;
	}

}
