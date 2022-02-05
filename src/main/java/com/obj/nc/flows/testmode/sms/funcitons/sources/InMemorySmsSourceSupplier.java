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

package com.obj.nc.flows.testmode.sms.funcitons.sources;

import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.SmsMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sources.SourceSupplierAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Optional;

@Component
@ConditionalOnMissingBean(type = "SmsSender")
public class InMemorySmsSourceSupplier extends SourceSupplierAdapter<SmsMessage> {
	
	public static final String ORIGINAL_RECIPIENTS_PHONE_ATTR_NAME = "ORIGINAL_RECIPIENTS_PHONE_NUM";
	public static final String ORIGINAL_RECIPIENTS_NAME_ATTR_NAME = "ORIGINAL_RECIPIENTS_NAME";

	
	private LinkedList<SmsMessage> received = new LinkedList<SmsMessage>();

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(SmsMessage payload) {
		return Optional.empty();
	}

	@Override
	protected SmsMessage execute() {
		if (received.isEmpty()) {
			return null;
		}
		
		SmsMessage sms= received.getFirst();
		
		SimpleTextContent content =sms.getBody();
		
		SmsEndpoint recipient = (SmsEndpoint)sms.getReceivingEndpoints().iterator().next();		

		content.setAttributeValue(ORIGINAL_RECIPIENTS_PHONE_ATTR_NAME, recipient.getPhone());
		// if (recipient.getRecipient()!=null) {
		// 	content.setAttributeValue(ORIGINAL_RECIPIENTS_NAME_ATTR_NAME, recipient.getRecipient().getName());
		// }
		
		received.removeFirst();
		
		return sms;
	}
	
	public void receive(SmsMessage msg) {
		received.addLast(msg);
	}
	
	public int getReceivedCount() {
		return received.size();
	}
	
	public void purgeAllReceivedMessages() {
		received = new LinkedList<>();
	}

}
