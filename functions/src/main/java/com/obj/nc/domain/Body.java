package com.obj.nc.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.obj.nc.domain.content.AggregatedEmail;
import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.content.SimpleText;
import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.RecievingEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class Body extends BaseJSONObject{

	private Content message = new SimpleText();
	
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
	
	public <T extends Content> T getContentTyped() {
		return (T) message;
	}

}
