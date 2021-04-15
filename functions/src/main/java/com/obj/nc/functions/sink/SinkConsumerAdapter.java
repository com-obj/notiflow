package com.obj.nc.functions.sink;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;

public abstract class SinkConsumerAdapter<IN> extends SinkConsumer<IN> {

	public PreCondition<IN> preCondition() {
		return (input) -> {return checkPreCondition(input);};
	}
	
	public Consumer<IN> execution() {
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

	protected abstract void execute(IN payload);

}
