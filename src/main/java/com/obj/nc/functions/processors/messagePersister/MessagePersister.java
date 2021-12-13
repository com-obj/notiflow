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

package com.obj.nc.functions.processors.messagePersister;

import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessagePersistentState;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.repositories.MessageRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MessagePersister extends ProcessorFunctionAdapter<Message<?>,Message<?>> {

    @Autowired
    private MessageRepository messageRepo;


	@Override
	protected Message<?> execute(Message<?> message) {
		MessagePersistentState persisted = messageRepo.save(message.toPersistentState());

		Message<?> reconstructed = persisted.toMessage();
		reconstructed.setReceivingEndpoints(message.getReceivingEndpoints());
		return reconstructed;
	}



}
