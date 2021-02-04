package com.obj.nc.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.RecievingEndpoint;

import com.obj.nc.domain.message.MessageContents;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class Body extends BaseJSONObject{

	private MessageContents message = new MessageContents();
	
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
		Body merged = new Body();
		merged.setAttributes(this.getAttributes());
		other.getAttributes().forEach((key, value) -> merged.getAttributes().putIfAbsent(key, value));

		merged.message = message;
		merged.message = merged.message.merge(other.message);

		merged.recievingEndpoints = recievingEndpoints;
		merged.deliveryOptions = deliveryOptions;

		return merged;
	}

}
