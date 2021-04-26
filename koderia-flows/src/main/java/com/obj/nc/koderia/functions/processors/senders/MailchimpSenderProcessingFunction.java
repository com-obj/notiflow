package com.obj.nc.koderia.functions.processors.senders;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.PreCondition;
import com.obj.nc.functions.processors.ProcessorFunction;

import lombok.AllArgsConstructor;

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
