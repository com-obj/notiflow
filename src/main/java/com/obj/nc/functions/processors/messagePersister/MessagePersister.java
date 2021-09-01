package com.obj.nc.functions.processors.messagePersister;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessagePersistantState;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.repositories.MessageRepository;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
public class MessagePersister extends ProcessorFunctionAdapter<Message<?>,Message<?>> {

    @Autowired
    private MessageRepository messageRepo;


	@Override
	protected Message<?> execute(Message<?> message) {
		if (messageRepo.findById(message.getId()).isPresent()) {
			log.info("Message with id {} is already in DB", message.getId());
			return message;
		}
		
		MessagePersistantState persisted = messageRepo.save(message.toPersistantState());
		return persisted.toMessage();
	}



}
