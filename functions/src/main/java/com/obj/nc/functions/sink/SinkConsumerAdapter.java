package com.obj.nc.functions.sink;

import java.util.Optional;
import java.util.function.Consumer;

import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;

public abstract class SinkConsumerAdapter<IN> extends SinkConsumer<IN> {

	public PreCondition<IN> preCondition() {
		return (input) -> {return checkPreCondition(input);};
	}
	
	public Consumer<IN> execution() {
		return (input) -> execute(input);
	}

	protected abstract Optional<PayloadValidationException> checkPreCondition(IN payload);

	protected abstract void execute(IN payload);

}
