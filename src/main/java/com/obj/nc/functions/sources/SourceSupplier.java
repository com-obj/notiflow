/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
