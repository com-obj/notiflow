package com.obj.nc.functions.sink.payloadLogger;

import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaylaodLoggerPreCondition implements PreCondition<Message> {

	@Override
	public Optional<PayloadValidationException> apply(Message message) {
		return Optional.empty();
	}

}