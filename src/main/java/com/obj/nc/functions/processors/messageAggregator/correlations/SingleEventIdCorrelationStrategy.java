package com.obj.nc.functions.processors.messageAggregator.correlations;

import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.messaging.Message;

import com.obj.nc.domain.headers.Header;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class SingleEventIdCorrelationStrategy implements CorrelationStrategy {

	@Override
	public Object getCorrelationKey(Message<?> m) {
		com.obj.nc.domain.message.Message message = ((com.obj.nc.domain.message.Message)m.getPayload());
		Object eventId = message.getEventIds().iterator().next();
		
		log.info("Correlating message EVENT_ID : "+ eventId);
		return eventId;
	}

}
