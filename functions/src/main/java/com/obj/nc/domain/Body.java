package com.obj.nc.domain;

import java.util.ArrayList;
import java.util.List;

import com.obj.nc.domain.message.Message;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class Body extends BaseJSONObject{

	private MessageContent message;
	
	private List<Attachement> attachments = new ArrayList<Attachement>();
	
	private List<Recipient> recipients = new ArrayList<Recipient>();
	
	private DeliveryOptions deliveryOptions = new DeliveryOptions();	

	public Body addRecipient(Recipient r) {
		this.recipients.add(r);
		return this;
	}

}
