package com.obj.nc.functions.sink.messagePersister;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.sink.SinkConsumerAdapter;
import com.obj.nc.repositories.MessageRepository;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
public class MessagePersister extends SinkConsumerAdapter<Message> {

    @Autowired
    private MessageRepository messageRepo;


	@Override
	protected void execute(Message message) {
		messageRepo.save(message);
	}



}
