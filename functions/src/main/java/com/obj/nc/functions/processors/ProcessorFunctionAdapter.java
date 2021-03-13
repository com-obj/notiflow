package com.obj.nc.functions.processors;

import java.util.Optional;
import java.util.function.Function;

import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;

public abstract class ProcessorFunctionAdapter<IN, OUT> extends ProcessorFunction<IN, OUT> {
	

	public PreCondition<IN> preCondition() {
		return (input) -> {return checkPreCondition(input);};
	}
	
	public Function<IN, OUT> execution() {
		return (input) -> execute(input);
	}
	
	protected abstract Optional<PayloadValidationException> checkPreCondition(IN payload);

	protected abstract OUT execute(IN payload);
}
