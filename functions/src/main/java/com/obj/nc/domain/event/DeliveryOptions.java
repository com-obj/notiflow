package com.obj.nc.domain.event;

public class DeliveryOptions {
	
	public enum AGGREGATION_TYPE {
		ONCE_A_DAY, ONCE_A_WEEK
	}
	
	AGGREGATION_TYPE aggregationType;

	public AGGREGATION_TYPE getAggregationType() {
		return aggregationType;
	}

	public void setAggregationType(AGGREGATION_TYPE aggregationType) {
		this.aggregationType = aggregationType;
	}
	
	

}
