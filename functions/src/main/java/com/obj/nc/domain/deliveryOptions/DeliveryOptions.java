package com.obj.nc.domain.deliveryOptions;

import lombok.Data;

@Data
public class DeliveryOptions {
	
//	private ChannelDeliveryOption channel;
	private AggregationOption aggregation;
	private SchedulingOption scheduling;
	
}
