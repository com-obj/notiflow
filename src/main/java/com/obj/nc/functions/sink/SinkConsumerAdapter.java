/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
