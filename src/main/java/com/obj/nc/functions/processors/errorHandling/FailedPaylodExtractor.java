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

package com.obj.nc.functions.processors.errorHandling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.config.SpringIntegration;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.flows.errorHandling.domain.FailedPayload;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FailedPaylodExtractor extends ProcessorFunctionAdapter<FailedPayload, Message<?>> {

	@Autowired @Qualifier(SpringIntegration.OBJECT_MAPPER_FOR_SPRING_MESSAGES_BEAN_NAME)
	private ObjectMapper jsonConverterForMessages;
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(FailedPayload payload) {
		return super.checkPreCondition(payload);
	}
	
	@Override
	@SneakyThrows
	protected Message<?> execute(FailedPayload failedPaylod) {
		Message<?> failedMsg = jsonConverterForMessages.treeToValue(failedPaylod.getMessageJson(), Message.class);
		return failedMsg;
	}

}
