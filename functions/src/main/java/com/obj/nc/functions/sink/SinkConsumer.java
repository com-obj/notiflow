package com.obj.nc.functions.sink;

import java.util.Optional;
import java.util.function.Consumer;

import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;

public abstract class SinkConsumer<IN> implements Consumer<IN> {

	@Override
	public void accept(IN input) {
		Optional<PayloadValidationException> error = preCondition().apply(input);

		if (error.isPresent()) {
			throw error.get();
		}

		execution().accept(input);
	}
	
	public abstract PreCondition<IN> preCondition();
	
	public abstract Consumer<IN> execution();

}
