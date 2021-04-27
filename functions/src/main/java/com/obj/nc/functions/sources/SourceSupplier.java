package com.obj.nc.functions.sources;

import java.util.Optional;
import java.util.function.Supplier;

import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;

public abstract class SourceSupplier<OUT> implements Supplier<OUT> {

	@Override
	public OUT get() {
		OUT toGet = execution().get();

		Optional<PayloadValidationException> error = preCondition().apply(toGet);

		if (error.isPresent()) {
			throw error.get();
		}

		return toGet;
	}

	public abstract PreCondition<OUT> preCondition();

	public abstract Supplier<OUT> execution();

}
