package com.obj.nc.koderia.functions.processors;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.PreCondition;
import com.obj.nc.functions.processors.ProcessorFunction;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class KoderiaRecipientsProcessingFunction extends ProcessorFunction<NotificationIntent, NotificationIntent> {

	@Autowired
	private KoderiaRecipientsExecution execution;

	@Autowired
	private KoderiaRecipientsPreCondition preCondition;

	@Override
	public PreCondition<NotificationIntent> preCondition() {
		return preCondition;
	}

	@Override
	public Function<NotificationIntent, NotificationIntent> execution() {
		return execution;
	}

}
