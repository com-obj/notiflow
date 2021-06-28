package com.obj.nc.functions.sources.message;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessagePersistantState;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sources.SourceSupplierAdapter;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Optional;

@DocumentProcessingInfo("InputMessageSupplier")
public class MessageSupplier extends SourceSupplierAdapter<Message<?>> {
	
	@Autowired
	private MessageRepository repository;

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(Message<?> payload) {
		return Optional.empty();
	}

	@Override
	protected Message<?> execute() {
		Optional<MessagePersistantState> persistedMessageToProcess = repository.findFirstByTimeConsumedIsNullOrderByTimeCreatedAsc();

		if (!persistedMessageToProcess.isPresent()) {
			return null;
		}
		
		Message<?> messageToProcess = persistedMessageToProcess.get().toMessage();
		messageToProcess.setTimeConsumed(Instant.now());
		repository.save(messageToProcess.toPersistantState());
		return messageToProcess;
	}

}
