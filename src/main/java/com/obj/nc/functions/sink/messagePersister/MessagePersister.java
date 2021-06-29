package com.obj.nc.functions.sink.messagePersister;

import com.obj.nc.exceptions.PayloadValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.sink.SinkConsumerAdapter;
import com.obj.nc.repositories.MessageRepository;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

@Component
@AllArgsConstructor
@Log4j2
public class MessagePersister extends SinkConsumerAdapter<Message<?>> {
	
	@Autowired private MessageRepository messageRepository;
	
	protected Optional<PayloadValidationException> checkPreCondition(Message<?> payload) {
		if (payload == null) {
			return Optional.of(new PayloadValidationException("Could not persist Message because its null. Payload: " + payload));
		}
		return Optional.empty();
	}
	
	protected void execute(Message<?> payload) {
		log.debug("Persisting message {}",payload);
		messageRepository.save(payload.toPersistantState());
	}
	
}
