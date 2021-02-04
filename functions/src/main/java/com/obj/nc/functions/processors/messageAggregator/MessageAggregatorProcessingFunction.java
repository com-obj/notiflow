package com.obj.nc.functions.processors.messageAggregator;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.ProcessorFunction;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
@AllArgsConstructor
public class MessageAggregatorProcessingFunction extends ProcessorFunction<List<Message>, Message> {
	@Autowired
	private MessageAggregatorExecution execution;

	@Autowired
	private MessageAggregatorPreCondition checkPreConditions;

	@Override
	public MessageAggregatorPreCondition preCondition() {
		return checkPreConditions;
	}

	@Override
	public Function<List<Message>, Message> execution() {
		return execution;
	}



}