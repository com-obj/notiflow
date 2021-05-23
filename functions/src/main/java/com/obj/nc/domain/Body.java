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
@Deprecated 
public abstract class Body extends BaseJSONObject implements HasRecievingEndpoints, HasDeliveryOptions {

	private Content message = new SimpleTextContent();
	
	//Ak je body sucastou message tak recievingEndpoints.size() = 1
	private List<RecievingEndpoint> recievingEndpoints = new ArrayList<RecievingEndpoint>();
	
	private DeliveryOptions deliveryOptions = new DeliveryOptions();	
	
	public Body addRecievingEndpoints(RecievingEndpoint ... r) {
		this.recievingEndpoints.addAll(Arrays.asList(r));
		return this;
	}
	
	@JsonIgnore
	public <T extends Content> T getContentTyped() {
		return (T) message;
	}
	
}
