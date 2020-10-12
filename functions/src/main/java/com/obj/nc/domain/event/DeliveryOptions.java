package com.obj.nc.domain.event;

import lombok.Data;

@Data
public class DeliveryOptions {
	
	public enum AGGREGATION_TYPE {
		ONCE_A_DAY, ONCE_A_WEEK
	}
	
	AGGREGATION_TYPE aggregationType;


}
