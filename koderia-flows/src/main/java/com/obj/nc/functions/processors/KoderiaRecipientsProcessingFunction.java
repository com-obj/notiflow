package com.obj.nc.functions.processors;

import com.obj.nc.domain.event.Event;
import com.obj.nc.functions.PreCondition;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@AllArgsConstructor
public class KoderiaRecipientsProcessingFunction extends ProcessorFunction<Event, Event> {

	@Autowired
	private KoderiaRecipientsExecution execution;

	@Autowired
	private KoderiaRecipientsPreCondition preCondition;

	@Override
	public PreCondition<Event> preCondition() {
		return preCondition;
	}

	@Override
	public Function<Event, Event> execution() {
		return execution;
	}

}
