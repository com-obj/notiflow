package com.obj.nc.functions.sink.payloadLogger;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.PreCondition;
import com.obj.nc.functions.sink.SinkConsumer;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@AllArgsConstructor
public class PaylaodLoggerSinkConsumer extends SinkConsumer<Message> {

	@Autowired
	private PaylaodLoggerExecution execution;

	@Autowired
	private PaylaodLoggerPreCondition preCondition;

	@Override
	public PreCondition<Message> preCondition() {
		return preCondition;
	}

	@Override
	public Consumer<Message> execution() {
		return execution;
	}

}
