package com.obj.nc.functions.processors.eventFactory;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.functions.PreCondition;
import com.obj.nc.functions.processors.ProcessorFunction;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class EventFactoryProcessingFunction extends ProcessorFunction<GenericEvent, Event> {

	@Autowired
	private EventFactoryExecution execution;

	@Autowired
	private EventFactoryPreCondition preCondition;

	@Override
	public PreCondition<GenericEvent> preCondition() {
		return preCondition;
	}

	@Override
	public Function<GenericEvent, Event> execution() {
		return execution;
	}

}
