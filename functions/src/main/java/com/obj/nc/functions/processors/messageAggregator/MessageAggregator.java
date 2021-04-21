package com.obj.nc.functions.processors.messageAggregator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.aggregator.AbstractAggregatingMessageGroupProcessor;
import org.springframework.integration.store.MessageGroup;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.messageAggregator.aggregations.PayloadAggregationStrategy;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public class MessageAggregator extends AbstractAggregatingMessageGroupProcessor {
	
	@Autowired private PayloadAggregationStrategy aggregationStrategy;

	@Override
	protected Object aggregatePayloads(MessageGroup group, Map<String, Object> defaultHeaders) {
		log.info("Starting aggregating message group of " +  group.getSequenceSize() + " messages");
		
		List<Message> messages = group.getMessages().stream().map(sm -> ((Message)sm.getPayload())).collect(Collectors.toList());


		return aggregationStrategy.merge(messages);
	}

}
