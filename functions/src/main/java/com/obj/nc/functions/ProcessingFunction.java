package com.obj.nc.functions;

import java.util.Optional;
import java.util.function.Function;

import com.obj.nc.exceptions.PayloadValidationException;

public abstract class ProcessingFunction<IN, OUT, COND extends PreCondition<IN>> implements Function<IN, OUT> {

	@Override
	public OUT apply(IN input) {
		Optional<PayloadValidationException> error = preCondition().apply(input);
		
		if (error.isPresent()) {
			throw error.get();
		}
		
		return execution().apply(input);
	}
	
	public abstract COND preCondition();
	
	public abstract Function<IN, OUT> execution();

}
