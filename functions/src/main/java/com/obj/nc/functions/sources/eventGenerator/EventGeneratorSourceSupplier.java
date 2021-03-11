package com.obj.nc.functions.sources.eventGenerator;

import com.obj.nc.domain.event.Event;
import com.obj.nc.functions.PreCondition;
import com.obj.nc.functions.sources.SourceSupplier;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@AllArgsConstructor
public class EventGeneratorSourceSupplier extends SourceSupplier<Event> {

	@Autowired
	private EventGeneratorExecution execution;

	@Autowired
	private EventGeneratorPreCondition preCondition;

	@Override
	public PreCondition<Event> preCondition() {
		return preCondition;
	}

	@Override
	public Supplier<Event> execution() {
		return execution;
	}

}
