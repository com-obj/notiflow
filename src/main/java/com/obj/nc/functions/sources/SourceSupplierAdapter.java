package com.obj.nc.functions.sources;

import java.util.Optional;
import java.util.function.Supplier;

import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;

public abstract class SourceSupplierAdapter<OUT> extends SourceSupplier<OUT> {

	public PreCondition<OUT> preCondition() {
		return (output) -> {return checkPreCondition(output);};
	}
	
	public Supplier<OUT> execution() {
		return () -> {return execute();};
	}

	protected abstract Optional<PayloadValidationException> checkPreCondition(OUT payload);

	protected abstract OUT execute();
}
