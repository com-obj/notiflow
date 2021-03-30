package com.obj.nc.functions.processors.dummy;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.PreCondition;
import com.obj.nc.functions.processors.ProcessorFunction;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@AllArgsConstructor
public class DummyRecepientsEnrichmentProcessingFunction extends ProcessorFunction<NotificationIntent, NotificationIntent> {

	@Autowired
	private DummyRecepientsEnrichmentExecution execution;

	@Autowired
	private DummyRecepientsEnrichmentPreCondition preCondition;

	@Override
	public PreCondition<NotificationIntent> preCondition() {
		return preCondition;
	}

	@Override
	public Function<NotificationIntent, NotificationIntent> execution() {
		return execution;
	}

}
