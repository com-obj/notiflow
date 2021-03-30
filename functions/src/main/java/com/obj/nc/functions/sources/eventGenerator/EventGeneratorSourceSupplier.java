package com.obj.nc.functions.sources.eventGenerator;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.PreCondition;
import com.obj.nc.functions.sources.SourceSupplier;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@AllArgsConstructor
public class EventGeneratorSourceSupplier extends SourceSupplier<NotificationIntent> {

	@Autowired
	private EventGeneratorExecution execution;

	@Autowired
	private EventGeneratorPreCondition preCondition;

	@Override
	public PreCondition<NotificationIntent> preCondition() {
		return preCondition;
	}

	@Override
	public Supplier<NotificationIntent> execution() {
		return execution;
	}

}
