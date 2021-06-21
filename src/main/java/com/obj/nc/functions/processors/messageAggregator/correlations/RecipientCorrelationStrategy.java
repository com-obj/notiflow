package com.obj.nc.functions.processors.messageAggregator.correlations;

import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.messaging.Message;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class RecipientCorrelationStrategy implements CorrelationStrategy {

	@Override
	public Object getCorrelationKey(Message<?> springMessage) {
		com.obj.nc.domain.message.Message<?> msg = (com.obj.nc.domain.message.Message<?>)springMessage.getPayload();
		
		String endpointId = msg.getRecievingEndpoints().iterator().next().getEndpointId();
		
		log.info("Correlating message using endpointId: "+ endpointId);
		return endpointId;
	}

}
