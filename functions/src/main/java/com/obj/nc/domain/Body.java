package com.obj.nc.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.RecievingEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class Body extends BaseJSONObject{

	private Content message = new SimpleTextContent();
	
	//Ak je body sucastou message tak recievingEndpoints.size() = 1
	//TODO: move to Header
	private List<RecievingEndpoint> recievingEndpoints = new ArrayList<RecievingEndpoint>();
	
	//TODO: move to Header
	private DeliveryOptions deliveryOptions = new DeliveryOptions();	

	public Body addRecievingEndpoints(RecievingEndpoint r) {
		this.recievingEndpoints.add(r);
		return this;
	}
	
	public Body addAllRecievingEndpoints(RecievingEndpoint ... r) {
		this.recievingEndpoints.addAll(Arrays.asList(r));
		return this;
	}
	
	@JsonIgnore
	public <T extends Content> T getContentTyped() {
		return (T) message;
	}
	
	public void copyMessage(Content message) {
		this.message = message.clone();
		this.message.copyAttributes(message.getAttributes());
	}
	
}
