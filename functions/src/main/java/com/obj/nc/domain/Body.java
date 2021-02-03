package com.obj.nc.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.MessageContent;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class Body extends BaseJSONObject{

	private MessageContent message = new MessageContent();
	
	private List<Attachement> attachments = new ArrayList<Attachement>();
	
	private List<RecievingEndpoint> recievingEndpoints = new ArrayList<RecievingEndpoint>();
	
	private DeliveryOptions deliveryOptions = new DeliveryOptions();	

	public Body addRecievingEndpoints(RecievingEndpoint r) {
		this.recievingEndpoints.add(r);
		return this;
	}
	
	public Body addAllRecievingEndpoints(RecievingEndpoint ... r) {
		this.recievingEndpoints.addAll(Arrays.asList(r));
		return this;
	}

	public Body merge(Body other) {
		Body merged = (Body) super.merge(other);
		merged.message.setNextMessage(other.getMessage());

		other.attachments.stream()
				.filter(attachement -> !attachments.contains(attachement))
				.forEach(attachments::add);

		other.recievingEndpoints.stream()
				.filter(recievingEndpoint -> !recievingEndpoints.contains(recievingEndpoint))
				.forEach(recievingEndpoints::add);

		return merged;
	}

}
