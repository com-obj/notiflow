package com.obj.nc.functions.processors.eventIdGenerator;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.processors.ProcessorFunction;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ValidateAndGenerateEventIdProcessingFunction extends ProcessorFunction<NotificationIntent, NotificationIntent> {
	@Autowired
	private ValidateAndGenerateEventIdExecution execution;

	@Autowired
	private ValidateAndGenerateEventIdPreCondition checkPreConditions;

	@Override
	public ValidateAndGenerateEventIdPreCondition preCondition() {
		return checkPreConditions;
	}

	@Override
	public Function<NotificationIntent, NotificationIntent> execution() {
		return execution;
	}



}