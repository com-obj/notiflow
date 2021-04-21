package com.obj.nc.functions.processors.messageAggregator.aggregations;

import com.obj.nc.domain.BasePayload;

import java.util.List;

/**
 * This is used to have clean interface which is usable as pure function without spring dependency. In common case it is used as a delegate from
 * org.springframework.integration.aggregator.MessageGroupProcessor which is used in the spring aggregator
 * @author ja
 *
 */
@FunctionalInterface
public interface BasePayloadAggregationStrategy {
	
	/**
	 * Process the given list of payloads. Implementations are free to return as few or as many payloads
	 * based on the invocation as needed. Common return types are return BasePayload or List<BasePayload>
	 */
	Object merge(List<? extends BasePayload> payloads);

}
