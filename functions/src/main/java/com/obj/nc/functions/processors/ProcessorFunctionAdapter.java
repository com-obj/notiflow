package com.obj.nc.functions.processors;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;

public abstract class ProcessorFunctionAdapter<IN, OUT> extends ProcessorFunction<IN, OUT> {
	

	public PreCondition<IN> preCondition() {
		return (input) -> {return checkPreCondition(input);};
	}
	
	public Function<IN, OUT> execution() {
		return (input) -> execute(input);
	}
	
	protected Optional<PayloadValidationException> checkPreCondition(IN payload) {
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		Set<ConstraintViolation<IN>> violations = validator.validate( payload );

		if (violations.size() == 0) {
			return Optional.empty();
		}
		
		ConstraintViolation<IN> error = violations.iterator().next();
		
		return Optional.of(new PayloadValidationException(error));
	}

	protected abstract OUT execute(IN payload);
}
