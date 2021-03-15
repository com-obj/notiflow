package com.obj.nc.functions.processors.messageAggregator;

import com.obj.nc.domain.Messages;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.ProcessorFunction;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
@AllArgsConstructor
public class MessageAggregatorProcessingFunction extends ProcessorFunction<Messages, Message> {
	@Autowired
	private MessageAggregatorExecution execution;

	@Autowired
	private MessageAggregatorPreCondition checkPreConditions;

	@Override
	public MessageAggregatorPreCondition preCondition() {
		return checkPreConditions;
	}

	@Override
	public Function<Messages, Message> execution() {
		return execution;
	}



}