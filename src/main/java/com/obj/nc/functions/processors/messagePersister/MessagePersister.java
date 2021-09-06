package com.obj.nc.functions.processors.messagePersister;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessagePersistentState;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.repositories.MessageRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MessagePersister extends ProcessorFunctionAdapter<Message<?>,Message<?>> {

    @Autowired
    private MessageRepository messageRepo;


	@Override
	protected Message<?> execute(Message<?> message) {
		MessagePersistentState persisted = messageRepo.save(message.toPersistentState());
		return persisted.toMessage();
	}



}
