package com.obj.nc.functions.processors;

import java.util.Optional;
import java.util.function.Function;

import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;

import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class ProcessorFunction<IN, OUT> implements Function<IN, OUT> {

	@Override
	public OUT apply(IN input) {
		OUT	returnValue = doPreConditionCheckAndExecute(input);

		return returnValue;
	}

	private OUT doPreConditionCheckAndExecute(IN input) {
		Optional<PayloadValidationException> error = preCondition().apply(input);
		
		if (error.isPresent()) {
			throw error.get();
		}
		
		return execution().apply(input);
	}
	
	public abstract PreCondition<IN> preCondition();
	
	public abstract Function<IN, OUT> execution();

}
 