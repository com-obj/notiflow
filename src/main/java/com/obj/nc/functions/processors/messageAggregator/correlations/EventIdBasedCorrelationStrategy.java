package com.obj.nc.functions.processors.messageAggregator.correlations;

import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.messaging.Message;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EventIdBasedCorrelationStrategy implements CorrelationStrategy {

	@Override
	public Object getCorrelationKey(Message<?> m) {
		com.obj.nc.domain.message.Message ncMessage = (com.obj.nc.domain.message.Message)m.getPayload();

		log.info("Correlating message {} based on their eventIds {}",m , ncMessage.getHeader().getEventIds());
		return ncMessage.getHeader().getEventIds();
	}

}
