package com.obj.nc.functions.processors;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.dto.EmitEventDto;
import com.obj.nc.functions.PreCondition;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@AllArgsConstructor
public class KoderiaEventConverterProcessingFunction extends ProcessorFunction<EmitEventDto, NotificationIntent> {

	@Autowired
	private KoderiaEventConverterExecution execution;

	@Autowired
	private KoderiaEventConverterPreCondition preCondition;

	@Override
	public PreCondition<EmitEventDto> preCondition() {
		return preCondition;
	}

	@Override
	public Function<EmitEventDto, NotificationIntent> execution() {
		return execution;
	}

}
