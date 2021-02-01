package com.obj.nc.functions.processors.senders;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.PreCondition;
import com.obj.nc.functions.processors.ProcessorFunction;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@AllArgsConstructor
public class EmailSenderSinkProcessingFunction extends ProcessorFunction<Message, Message> {

	@Autowired
	private EmailSenderSinkExecution execution;

	@Autowired
	private EmailSenderSinkPreCondition preCondition;

	@Override
	public PreCondition<Message> preCondition() {
		return preCondition;
	}

	@Override
	public Function<Message, Message> execution() {
		return execution;
	}

}
