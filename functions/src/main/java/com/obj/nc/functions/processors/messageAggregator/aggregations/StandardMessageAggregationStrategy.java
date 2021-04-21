package com.obj.nc.functions.processors.messageAggregator.aggregations;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.obj.nc.domain.Body;
import com.obj.nc.domain.content.AggregatedContent;
import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class StandardMessageAggregationStrategy extends ProcessorFunctionAdapter<List<Message>, Message> implements PayloadAggregationStrategy {

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(List<Message> messages) {
		if (messages.isEmpty()) {
			return Optional.of(new PayloadValidationException("There are no input messages to process"));
		}
		
		if (!isAllDeliveryOptionAggregated(messages)) {
			return Optional.of(new PayloadValidationException("Input message is not intended to be aggregated"));
		}

		Stream<DeliveryOptions> deliveryOptions = messages.stream()
				.map(Message::getBody)
				.map(Body::getDeliveryOptions);

		if (!deliveryOptions.allMatch(messages.get(0).getBody().getDeliveryOptions()::equals)) {
			return Optional.of(new PayloadValidationException("Messages do not share the same delivery options"));
		}

		Stream<List<RecievingEndpoint>> recievingEndpoints = messages.stream()
				.map(Message::getBody)
				.map(Body::getRecievingEndpoints);

		if (!recievingEndpoints.allMatch(messages.get(0).getBody().getRecievingEndpoints()::equals)) {
			return Optional.of(new PayloadValidationException("Messages do not share the same receiving endpoints"));
		}

		return Optional.empty();
	}

	@Override
	protected Message execute(List<Message> messages) {
		Message outputMessage = Message.createAsAggregatedContent();
//		outputMessage.getBody().setRecievingEndpoints(messages.getMessages().get(0).getBody().getRecievingEndpoints());
//		outputMessage.getBody().setDeliveryOptions(messages.getMessages().get(0).getBody().getDeliveryOptions());

		AggregatedContent aggregatedContent = outputMessage.getContentTyped();
		for (Message msg : messages) {
			aggregatedContent.add(msg.getContentTyped());
		}

		return outputMessage;
	}
	
	@Override
	public Message merge(List<Message> messages) {
		return execute(messages);
	}
	
	private boolean isAllDeliveryOptionAggregated(List<Message> messages) {
		return messages
				.stream()
				.allMatch(msg -> msg.getBody().getDeliveryOptions().getAggregationType() != DeliveryOptions.AGGREGATION_TYPE.NONE);
	}

}