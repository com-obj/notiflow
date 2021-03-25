package com.obj.nc.functions.processors.messageAggregator.correlations;

import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.messaging.Message;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TestModeCorrelationStrategy implements CorrelationStrategy {

	@Override
	public Object getCorrelationKey(Message<?> m) {
		log.info("Correlating message while in test mode: "+ m);
		return true;
	}

}
