package com.obj.nc.functions.sink.inputPersister;

import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sink.SinkConsumerAdapter;
import com.obj.nc.repositories.MessageRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
@Log4j2
public class MessagePersisterConsumer extends SinkConsumerAdapter<Message<?>> {
    
    @Autowired 
    private MessageRepository messageRepository;
    
	protected Optional<PayloadValidationException> checkPreCondition(Message<?> payload) {
		if (payload == null) {
			return Optional.of(new PayloadValidationException("Could not persist Message because its null. Payload: " + payload));
		}
		if (payload.getMessageId()==null) {
			return Optional.of(new PayloadValidationException("Could not persist Message because messageId is null. Payload: " + payload));
		}

		return Optional.empty();
	}
	
    protected void execute(Message<?> payload) {
        log.debug("Persisting generic event {}",payload);
        messageRepository.save(payload.toPersistantState());
    }
    
}
