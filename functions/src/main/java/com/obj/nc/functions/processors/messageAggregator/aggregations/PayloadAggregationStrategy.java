package com.obj.nc.functions.processors.messageAggregator.aggregations;

import com.obj.nc.domain.message.Message;

import java.util.List;

/**
 * This is used to have clean interface which is usable as pure function without spring dependency. In common case it is used as a delegate from
 * org.springframework.integration.aggregator.MessageGroupProcessor which is used in the spring aggregator
 * @author ja
 *
 */
public interface PayloadAggregationStrategy {
	
	public Message merge(List<Message> messages);

}
