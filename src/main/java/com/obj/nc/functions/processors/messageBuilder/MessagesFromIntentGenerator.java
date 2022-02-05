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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Collections2;
import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.content.TemplateWithModelContent;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.content.mailchimp.TemplatedMailchimpContent;
import com.obj.nc.domain.content.push.PushContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.EmailMessageTemplated;
import com.obj.nc.domain.message.MailchimpMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.PushMessage;
import com.obj.nc.domain.message.SmsMessage;
import com.obj.nc.domain.message.SmsMessageTemplated;
import com.obj.nc.domain.message.TemplatedMailchimpMessage;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.domain.recipients.Recipient;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.extensions.providers.recipients.ContactsProvider;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
@DocumentProcessingInfo("GenerateMessagesFromIntent")
public class MessagesFromIntentGenerator extends ProcessorFunctionAdapter<NotificationIntent, List<? extends Message<?>>> {

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(NotificationIntent notificationIntent) {
		//If there is a way to subscribe to intent, this would be ok
		if (notificationIntent.getRecipients().isEmpty()) {
			return Optional.of(new PayloadValidationException(
					String.format("NotificationIntent %s has no recipients defined.", notificationIntent)));
		}

		return Optional.empty();
	}

	@Override
	protected List<? extends Message<?>> execute(NotificationIntent notificationIntent) {
		log.debug("Create messages for {}",  notificationIntent);
		
		// TODO different settings can apply here based on delivery options like if we are outside business hours, convert to email. otherwise convert to SMS
		List<Message<?>> messages = new ArrayList<>();		

		for (Recipient recipient: notificationIntent.getRecipients()) {

			List<ReceivingEndpoint> endpoints = recipient.getReceivingEndpoints();

			if (CollectionUtils.isEmpty(endpoints)) {
				log.warn("Intent to message generator didn't find endpoint for Recipient {}. This means that Notiflow cannot route Intent for this recipient", recipient);
			}
			if (endpoints.size()>1) {
				// TODO 
				log.warn("Notiflow in current version picks first endpoint for each recipient. This will be enhanced in the future", recipient);
			}
			
			ReceivingEndpoint endpoint = endpoints.iterator().next();
			Message<?> msg = (Message<?>) createMessageForEndpoint(notificationIntent, endpoint);
			
			msg.addReceivingEndpoints(endpoint);

			msg.setAttributes(notificationIntent.getAttributes());
			messages.add(msg);
		}

		return messages;
	}

	public Message<?> createMessageForEndpoint(NotificationIntent intent, ReceivingEndpoint forEndpoint) {
		MessageContent msgContent = intent.getBody().createMessageContent(forEndpoint);

		if (msgContent instanceof EmailContent) {
			EmailMessage email = Message.newTypedMessageFrom(EmailMessage.class, intent);
			email.setBody((EmailContent)msgContent);
			
			return email;
		} 
		
		if (msgContent instanceof SimpleTextContent) {
			SmsMessage sms = Message.newTypedMessageFrom(SmsMessage.class, intent);
			sms.setBody((SimpleTextContent)msgContent);
			
			return sms;
		} 
		
		if (msgContent instanceof TemplateWithModelContent && forEndpoint instanceof EmailEndpoint) {
			EmailMessageTemplated<?> email = Message.newTypedMessageFrom(EmailMessageTemplated.class, intent);
			email.setBody((TemplateWithModelEmailContent)msgContent);
			
			return email;
		} 
		
		if (msgContent instanceof TemplateWithModelContent && forEndpoint instanceof SmsEndpoint) {
			SmsMessageTemplated<?> sms = Message.newTypedMessageFrom(SmsMessageTemplated.class, intent);
			sms.setBody((TemplateWithModelContent)msgContent);
			
			return sms;
		}
		
		if (msgContent instanceof TemplatedMailchimpContent) {
			TemplatedMailchimpMessage mailChimp = Message.newTypedMessageFrom(TemplatedMailchimpMessage.class, intent);
			mailChimp.setBody((TemplatedMailchimpContent)msgContent);
			
			return mailChimp;
		}
		
		if (msgContent instanceof MailchimpContent) {
			MailchimpMessage mailChimp = Message.newTypedMessageFrom(MailchimpMessage.class, intent);
			mailChimp.setBody((MailchimpContent)msgContent);
			
			return mailChimp;
		}
		
		if (msgContent instanceof PushContent) {
			PushMessage push = Message.newTypedMessageFrom(PushMessage.class, intent);
			push.setBody((PushContent)msgContent);
			
			return push;
		}

		throw new NotImplementedException("Add additional cases in createMessageForEndpoint for endpoint type " + forEndpoint.getClass().getSimpleName());

	}

}
