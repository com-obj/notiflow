package com.obj.nc.functions.processors.messageAggregator.aggregations;

import com.obj.nc.domain.Messages;
import com.obj.nc.domain.message.Message;

public interface MessageAggregationStrategy {
	
	public Message merge(Messages messages);

}
