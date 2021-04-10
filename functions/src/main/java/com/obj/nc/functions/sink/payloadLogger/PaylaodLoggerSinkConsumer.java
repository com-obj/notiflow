package com.obj.nc.functions.sink.payloadLogger;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sink.SinkConsumerAdapter;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
public class PaylaodLoggerSinkConsumer extends SinkConsumerAdapter<Message> {

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(Message payload) {
		return Optional.empty();
	}

	@Override
	protected void execute(Message payload) {
		log.info(payload.toString());
	}

}
