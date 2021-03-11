package com.obj.nc.functions.processors.messageAggregator;

import com.obj.nc.domain.Body;
import com.obj.nc.domain.Messages;
import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class MessageAggregatorPreCondition implements PreCondition<Messages> {

	@Override
	public Optional<PayloadValidationException> apply(Messages messages) {
		if (messages.getMessages().isEmpty()) {
			return Optional.of(new PayloadValidationException("There are no input messages to process"));
		}

		if (!messages.getMessages().stream().allMatch(Message::isAggregateMessage)) {
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

}