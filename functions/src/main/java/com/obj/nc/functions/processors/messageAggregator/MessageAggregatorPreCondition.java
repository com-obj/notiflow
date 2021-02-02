package com.obj.nc.functions.processors.messageAggregator;

import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class MessageAggregatorPreCondition implements PreCondition<List<Message>> {

	@Override
	public Optional<PayloadValidationException> apply(List<Message> messages) {
		if (!messages.stream().allMatch(Message::isAggregateMessage)) {
			return Optional.of(new PayloadValidationException("Input message is not intended to be aggregated"));
		}

		return Optional.empty();
	}

}