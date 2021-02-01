package com.obj.nc.functions.processors.eventGenerator;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.event.Event;
import com.obj.nc.functions.ProcessorFunction;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ValidateAndGenerateEventIdProcessingFunction extends ProcessorFunction<Event, Event> {
	@Autowired
	private ValidateAndGenerateEventIdExecution execution;

	@Autowired
	private ValidateAndGenerateEventIdPreCondition checkPreConditions;

	@Override
	public ValidateAndGenerateEventIdPreCondition preCondition() {
		return checkPreConditions;
	}

	@Override
	public Function<Event, Event> execution() {
		return execution;
	}



}