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

package com.obj.nc.functions.sink.payloadLogger;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sink.SinkConsumerAdapter;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
public class PaylaodLoggerSinkConsumer extends SinkConsumerAdapter<Object> {

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(Object payload) {
		return Optional.empty();
	}

	@Override
	protected void execute(Object payload) {
		log.info(payload.toString());
	}

}
