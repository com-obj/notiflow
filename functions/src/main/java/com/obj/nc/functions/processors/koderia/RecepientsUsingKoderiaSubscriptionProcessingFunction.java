package com.obj.nc.functions.processors.koderia;

import com.obj.nc.domain.event.Event;
import com.obj.nc.functions.PreCondition;
import com.obj.nc.functions.processors.ProcessorFunction;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@AllArgsConstructor
public class RecepientsUsingKoderiaSubscriptionProcessingFunction extends ProcessorFunction<Event, Event> {

	@Autowired
	private RecepientsUsingKoderiaSubscriptionExecution execution;

	@Autowired
	private RecepientsUsingKoderiaSubscriptionPreCondition preCondition;

	@Override
	public PreCondition<Event> preCondition() {
		return preCondition;
	}

	@Override
	public Function<Event, Event> execution() {
		return execution;
	}

}
