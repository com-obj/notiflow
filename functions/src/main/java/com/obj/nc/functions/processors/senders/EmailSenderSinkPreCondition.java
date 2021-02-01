package com.obj.nc.functions.processors.senders;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EmailSenderSinkPreCondition implements PreCondition<Message> {

	@Override
	public Optional<PayloadValidationException> apply(Message message) {
		List<RecievingEndpoint> to = message.getBody().getRecievingEndpoints();

		if (to.size() != 1) {
			return Optional.of(new PayloadValidationException("Email sender can send to only one recipient. Found more: " + to));
		}

		RecievingEndpoint endpoint = to.get(0);
		if (!(endpoint instanceof EmailEndpoint)) {
			return Optional.of(new PayloadValidationException("Email sender can send to Email endpoints only. Found " + endpoint));
		}

		return Optional.empty();
	}

}