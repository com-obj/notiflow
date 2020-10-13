package com.obj.nc.domain.event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Header extends BaseJSONObject {
	
	@JsonProperty("configuration-name")
	private String configurationName;
		
	@NotNull
	@Include
	private UUID eventId;
	
	private List<Recipient> recipients = new ArrayList<Recipient>();
	
	private DeliveryOptions deliveryOptions = new DeliveryOptions();	

	public Header addRecipient(Recipient r) {
		this.recipients.add(r);
		return this;
	}

	public void generateAndSetEventID() {
		eventId = generateUUID();
	}

}