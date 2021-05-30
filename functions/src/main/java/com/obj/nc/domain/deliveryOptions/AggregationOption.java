package com.obj.nc.domain.deliveryOptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class AggregationOption extends BaseDeliveryOption {

	public enum AGGREGATION_TYPE {
		NONE, ONCE_A_DAY, ONCE_A_WEEK
	}
	
	private AGGREGATION_TYPE aggregationType = AGGREGATION_TYPE.NONE;
}
