package com.obj.nc.functions;

import java.util.Optional;
import java.util.function.Function;

import com.obj.nc.exceptions.PayloadValidationException;

public interface PreCondition<IN> extends Function<IN, Optional<PayloadValidationException>> {

}
