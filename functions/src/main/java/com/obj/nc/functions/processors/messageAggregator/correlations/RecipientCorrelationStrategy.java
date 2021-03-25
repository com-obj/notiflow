package com.obj.nc.functions.processors.messageAggregator.correlations;

import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.messaging.Message;

import com.obj.nc.domain.Body;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class RecipientCorrelationStrategy implements CorrelationStrategy {

	@Override
	public Object getCorrelationKey(Message<?> m) {
		Body body = ((com.obj.nc.domain.message.Message)m.getPayload()).getBody();
		String endpointId = body.getRecievingEndpoints().iterator().next().getEndpointId();
		
		log.info("Correlating message using endpointId: "+ endpointId);
		return endpointId;
	}

}
