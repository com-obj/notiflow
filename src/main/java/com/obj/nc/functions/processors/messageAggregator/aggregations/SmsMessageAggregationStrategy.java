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

package com.obj.nc.functions.processors.messageAggregator.aggregations;

import java.util.List;
import java.util.Optional;

import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.SmsMessage;
import com.obj.nc.exceptions.PayloadValidationException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SmsMessageAggregationStrategy extends BasePayloadAggregationStrategy<SimpleTextContent> {
	
	public static final String TEXT_CONCAT_DELIMITER = "\n\n";
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(List<Message<SimpleTextContent>> payloads) {
		Optional<PayloadValidationException> exception = checkContentTypes(payloads, SimpleTextContent.class);
		if (exception.isPresent()) {
			return exception;
		}
		
		return checkReceivingEndpoints(payloads);
	}
	
	@Override
	protected Optional<PayloadValidationException> checkReceivingEndpoints(List<Message<SimpleTextContent>> payloads) {
		Optional<PayloadValidationException> exception = checkEndpointTypes(payloads, SmsEndpoint.class);
		if (exception.isPresent()) {
			return exception;
		}
		
		return super.checkReceivingEndpoints(payloads);
	}
	
	@Override
	public Object merge(List<Message<SimpleTextContent>> payloads) {
		if (payloads.isEmpty()) return null;
		

		SimpleTextContent aggregatedSmsContent = payloads
				.stream()
				.map(Message::getBody)
				.reduce(this::concatContents)
				.orElseThrow(() -> new RuntimeException(String.format("Could not aggregate input messages: %s", payloads)));
		
		SmsMessage outputMessage = new SmsMessage();
		outputMessage.setBody(aggregatedSmsContent);
		return outputMessage;
	}
	
	private SimpleTextContent concatContents(SimpleTextContent a, SimpleTextContent b) {
		SimpleTextContent concated = new SimpleTextContent();
		concated.setText(a.getText().concat(TEXT_CONCAT_DELIMITER).concat(b.getText()));
		return concated;
	}

}