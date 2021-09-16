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

package com.obj.nc.functions.processors.messageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
@DocumentProcessingInfo("MessageByRecipientTokenizer")
public class MessageByRecipientTokenizer<CONTENT_TYPE extends MessageContent> extends ProcessorFunctionAdapter<Message<CONTENT_TYPE>, List<Message<CONTENT_TYPE>>> {

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(Message<CONTENT_TYPE> notificationIntent) {

		if (notificationIntent.getReceivingEndpoints().isEmpty()) {
			return Optional.of(new PayloadValidationException(
					String.format("NotificationIntent %s has no receiving endpoints defined.", notificationIntent)));
		}

		return Optional.empty();
	}

	@Override
	@SneakyThrows
	protected List<Message<CONTENT_TYPE>> execute(Message<CONTENT_TYPE> msg) {
		log.debug("Tokenizing message {} for recipients",  msg);

		List<Message<CONTENT_TYPE>> messages = new ArrayList<>();

		for (ReceivingEndpoint receivingEndpoint: msg.getReceivingEndpoints()) {

			Message<CONTENT_TYPE> msgClone = Message.newTypedMessageFrom(msg.getClass(), msg);
			
			msgClone.addReceivingEndpoints(receivingEndpoint);

			msgClone.setAttributes(msg.getAttributes());
			msgClone.setBody(msg.getBody());
			messages.add(msgClone);
		}

		return messages;
	}
}
