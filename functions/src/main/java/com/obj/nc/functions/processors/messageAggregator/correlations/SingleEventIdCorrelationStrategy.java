package com.obj.nc.functions.processors.messageAggregator.correlations;

import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.messaging.Message;

import com.obj.nc.domain.Header;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class SingleEventIdCorrelationStrategy implements CorrelationStrategy {

	@Override
	public Object getCorrelationKey(Message<?> m) {
		Header header = ((com.obj.nc.domain.message.Message)m.getPayload()).getHeader();
		Object eventId = header.getEventIds().iterator().next();
		
		log.info("Correlating message EVENT_ID : "+ eventId);
		return eventId;
	}

}
