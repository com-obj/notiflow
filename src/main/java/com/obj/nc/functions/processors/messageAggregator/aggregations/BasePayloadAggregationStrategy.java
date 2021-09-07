package com.obj.nc.functions.processors.messageAggregator.aggregations;

import java.util.List;
import java.util.Optional;

import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

/**
 * This is used to have clean interface which is usable as pure function without spring dependency. In common case it is used as a delegate from
 * org.springframework.integration.aggregator.MessageGroupProcessor which is used in the spring aggregator
 * @author ja
 *
 */
public abstract class BasePayloadAggregationStrategy<CONTENT_TYPE extends MessageContent> extends ProcessorFunctionAdapter<List<Message<CONTENT_TYPE>>, Object> {
	
	/**
	 * Process the given list of payloads. Implementations are free to return as few or as many payloads
	 * based on the invocation as needed. Common return types are return BasePayload or List<BasePayload>
	 */
	abstract Object merge(List<Message<CONTENT_TYPE>> payloads);
	
	@Override
	protected Object execute(List<Message<CONTENT_TYPE>> payload) {
		return this.merge(payload);
	}
	
//	protected Optional<PayloadValidationException> checkDeliveryOptions(List<Message<CONTENT_TYPE>> payloads) {
//		Optional<Message<CONTENT_TYPE>> firstPayload = payloads.stream().findFirst();
//		if (!firstPayload.isPresent()) {
//			return Optional.empty();
//		}
//				
//		Optional<Message<CONTENT_TYPE>> invalidPayload = payloads.stream()
//				.filter(payload -> !firstPayload.get().getDeliveryOptions().equals(payload.getDeliveryOptions()))
//				.findFirst();
//		
//		return invalidPayload.map(payload -> new PayloadValidationException(
//				String.format("Payload %s has different delivery options to other payloads. Is %s", payload, 
//						payload.getDeliveryOptions())));
//	}
	
	protected Optional<PayloadValidationException> checkReceivingEndpoints(List<Message<CONTENT_TYPE>> payloads) {
		Optional<Message<CONTENT_TYPE>> firstPayload = payloads.stream().findFirst();
		if (!firstPayload.isPresent()) {
			return Optional.empty();
		}
		
		Optional<Message<CONTENT_TYPE>> invalidPayload = payloads.stream()
				.filter(payload -> !firstPayload.get().getReceivingEndpoints().equals(payload.getReceivingEndpoints()))
				.findFirst();
		
		return invalidPayload.map(payload -> new PayloadValidationException(
				String.format("Payload %s has different recipients to other payloads. Is %s", payload,
						payload.getReceivingEndpoints())));
	}
	
	protected Optional<PayloadValidationException> checkContentTypes(List<Message<CONTENT_TYPE>> payloads, Class<CONTENT_TYPE> clazz) {
		Optional<Message<CONTENT_TYPE>> invalidPayload = payloads.stream()
				.filter(payload -> !clazz.isInstance(payload.getBody()))
				.findFirst();
		
		return invalidPayload.map(payload -> new PayloadValidationException(
				String.format("Payload %s has content of invalid type. Is %s", payload, payload.getBody().getClass().getName())));
	}
	
	protected Optional<PayloadValidationException> checkEndpointTypes(List<Message<CONTENT_TYPE>> payloads, Class<? extends ReceivingEndpoint> clazz) {
		for (Message<CONTENT_TYPE> payload : payloads) {
			Optional<? extends ReceivingEndpoint> invalidEndpoint = payload.getReceivingEndpoints().stream()
					.filter(endpoint -> !clazz.isInstance(endpoint))
					.findFirst();
			
			if (invalidEndpoint.isPresent()) {
				return Optional.of(new PayloadValidationException(String.format("Payload %s has endpoint of invalid type. Is %s", payload,
						invalidEndpoint.get().getClass().getName())));
			}
		}
		
		return Optional.empty();
	}

}
