package com.obj.nc.domain;

import lombok.Data;

@Data
public class DeliveryOptions {
	
	public enum AGGREGATION_TYPE {
		NONE, ONCE_A_DAY, ONCE_A_WEEK
	}
	
	public enum TIME_CONSTRAINT_TYPE {
		IMMEDIATE, CRON, NOT_BEFOR
	}
	
	AGGREGATION_TYPE aggregationType = AGGREGATION_TYPE.NONE;
	TIME_CONSTRAINT_TYPE  schedulingType = TIME_CONSTRAINT_TYPE.IMMEDIATE;


}
