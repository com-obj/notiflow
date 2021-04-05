package com.obj.nc.koderia.functions.processors.senders;

import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.PreCondition;
import com.obj.nc.functions.processors.ProcessorFunction;
import com.obj.nc.koderia.dto.EmitEventDto;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@AllArgsConstructor
public class MailchimpSenderProcessingFunction extends ProcessorFunction<Message, Message> {

	@Autowired
	private MailchimpSenderExecution execution;

	@Autowired
	private MailchimpSenderPreCondition preCondition;

	@Override
	public PreCondition<Message> preCondition() {
		return preCondition;
	}

	@Override
	public Function<Message, Message> execution() {
		return execution;
	}

}
