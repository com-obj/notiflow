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

package com.obj.nc.functions.processors.messageTemplating;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.content.TemplateWithModelContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.SmsMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.messageTemplating.config.ThymeleafConfiguration;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

import java.util.Locale;
import java.util.Optional;

@Component
@DocumentProcessingInfo("SmsFormatter")
public class SmsTemplateFormatter extends BaseTemplateFormatter<TemplateWithModelContent<?>, SimpleTextContent> {

	public SmsTemplateFormatter(TemplateEngine templateEngine, ThymeleafConfiguration config) {
		super(templateEngine, config);
	}

	@Override
	public Optional<PayloadValidationException> checkPreCondition(Message<TemplateWithModelContent<?>> message) {
		MessageContent content = message.getBody();
		
		if (!(content instanceof  TemplateWithModelContent)) {
			return Optional.of(new PayloadValidationException("SmsTemplateFormatter cannot format message because its content is not of type TemplateWithJsonModelSmsContent. Instead is " +  content.getClass().getSimpleName()));
		}		

		return Optional.empty();
	}

	protected Message<SimpleTextContent> createMessageWithFormattedContent(String formatedContent, Locale locale,  Message<TemplateWithModelContent<?>> payload) {		
		SmsMessage smsMessage = Message.newTypedMessageFrom(SmsMessage.class, payload);

		smsMessage.getBody().setText(formatedContent.trim());
		
		return smsMessage;
	}

}
