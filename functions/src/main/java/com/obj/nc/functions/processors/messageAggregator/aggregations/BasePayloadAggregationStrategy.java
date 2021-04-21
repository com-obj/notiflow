package com.obj.nc.functions.processors.messageAggregator.aggregations;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import java.util.List;
import java.util.Optional;

/**
 * This is used to have clean interface which is usable as pure function without spring dependency. In common case it is used as a delegate from
 * org.springframework.integration.aggregator.MessageGroupProcessor which is used in the spring aggregator
 * @author ja
 *
 */
public abstract class BasePayloadAggregationStrategy extends ProcessorFunctionAdapter<List<? extends BasePayload>, Object> {
	
	/**
	 * Process the given list of payloads. Implementations are free to return as few or as many payloads
	 * based on the invocation as needed. Common return types are return BasePayload or List<BasePayload>
	 */
	abstract Object merge(List<? extends BasePayload> payloads);
	
	@Override
	protected Object execute(List<? extends BasePayload> payload) {
		return this.merge(payload);
	}
	
	protected Optional<PayloadValidationException> checkDeliveryOptions(List<? extends BasePayload> payloads) {
		Optional<? extends BasePayload> firstPayload = payloads.stream().findFirst();
		if (!firstPayload.isPresent()) {
			return Optional.empty();
		}
		
		DeliveryOptions.AGGREGATION_TYPE firstMessageAggregationType = firstPayload.get().getBody().getDeliveryOptions().getAggregationType();
		if (DeliveryOptions.AGGREGATION_TYPE.NONE.equals(firstMessageAggregationType)) {
			return Optional.of(new PayloadValidationException(
					String.format("Payload %s has invalid aggregation type. Is %s", firstPayload.get(), firstMessageAggregationType)));
		}
		
		Optional<? extends BasePayload> invalidPayload = payloads.stream()
				.filter(payload -> !firstPayload.get().getBody().getDeliveryOptions().equals(payload.getBody().getDeliveryOptions()))
				.findFirst();
		
		return invalidPayload.map(payload -> new PayloadValidationException(
				String.format("Payload %s has different delivery options to other payloads. Is %s", payload, 
						payload.getBody().getDeliveryOptions().getAggregationType())));
	}
	
	protected Optional<PayloadValidationException> checkReceivingEndpoints(List<? extends BasePayload> payloads) {
		Optional<? extends BasePayload> firstPayload = payloads.stream().findFirst();
		if (!firstPayload.isPresent()) {
			return Optional.empty();
		}
		
		Optional<? extends BasePayload> invalidPayload = payloads.stream()
				.filter(payload -> !firstPayload.get().getBody().getRecievingEndpoints().equals(payload.getBody().getRecievingEndpoints()))
				.findFirst();
		
		return invalidPayload.map(payload -> new PayloadValidationException(
				String.format("Payload %s has different recipients to other payloads. Is %s", payload,
						payload.getBody().getRecievingEndpoints())));
	}
	
	protected Optional<PayloadValidationException> checkContentTypes(List<? extends BasePayload> payloads, Class<?> clazz) {
		Optional<? extends BasePayload> invalidPayload = payloads.stream().filter(payload -> !clazz.isInstance(payload.getBody().getMessage()))
				.findFirst();
		
		return invalidPayload.map(payload -> new PayloadValidationException(
				String.format("Payload %s has content of invalid type. Is %s", payload, payload.getBody().getMessage().getClass().getName())));
		
	}
	
	protected Optional<PayloadValidationException> checkEndpointTypes(List<? extends BasePayload> payloads, Class<?> clazz) {
		for (BasePayload payload : payloads) {
			Optional<? extends RecievingEndpoint> invalidEndpoint = payload.getBody().getRecievingEndpoints().stream()
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
