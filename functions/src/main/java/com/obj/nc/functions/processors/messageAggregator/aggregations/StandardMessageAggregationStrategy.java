package com.obj.nc.functions.processors.messageAggregator.aggregations;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.obj.nc.domain.Body;
import com.obj.nc.domain.Messages;
import com.obj.nc.domain.content.AggregatedContent;
import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class StandardMessageAggregationStrategy extends ProcessorFunctionAdapter<Messages, Message> implements PayloadAggregationStrategy {

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(Messages messages) {
		if (messages.isEmpty()) {
			return Optional.of(new PayloadValidationException("There are no input messages to process"));
		}
		
		if (!messages.isAllDeliveryOptionAggregated()) {
			return Optional.of(new PayloadValidationException("Input message is not intended to be aggregated"));
		}

		Stream<DeliveryOptions> deliveryOptions = messages.getMessages().stream()
				.map(Message::getBody)
				.map(Body::getDeliveryOptions);

		if (!deliveryOptions.allMatch(messages.getMessages().get(0).getBody().getDeliveryOptions()::equals)) {
			return Optional.of(new PayloadValidationException("Messages do not share the same delivery options"));
		}

		Stream<List<RecievingEndpoint>> recievingEndpoints = messages.getMessages().stream()
				.map(Message::getBody)
				.map(Body::getRecievingEndpoints);

		if (!recievingEndpoints.allMatch(messages.getMessages().get(0).getBody().getRecievingEndpoints()::equals)) {
			return Optional.of(new PayloadValidationException("Messages do not share the same receiving endpoints"));
		}

		return Optional.empty();
	}

	@Override
	protected Message execute(Messages messages) {
		Message outputMessage = Message.createAsAggregatedContent();
//		outputMessage.getBody().setRecievingEndpoints(messages.getMessages().get(0).getBody().getRecievingEndpoints());
//		outputMessage.getBody().setDeliveryOptions(messages.getMessages().get(0).getBody().getDeliveryOptions());

		AggregatedContent aggregatedContent = outputMessage.getContentTyped();
		for (Message msg : messages.getMessages()) {
			aggregatedContent.add(msg.getContentTyped());
		}

		return outputMessage;
	}
	
	@Override
	public Message merge(Messages messages) {
		return execute(messages);
	}

}