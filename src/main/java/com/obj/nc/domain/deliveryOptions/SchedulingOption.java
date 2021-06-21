package com.obj.nc.domain.deliveryOptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class SchedulingOption extends BaseDeliveryOption {

	public enum TIME_CONSTRAINT_TYPE {
		IMMEDIATE, NOT_BEFORE
	}
	
	private TIME_CONSTRAINT_TYPE  schedulingType = TIME_CONSTRAINT_TYPE.IMMEDIATE;
}
