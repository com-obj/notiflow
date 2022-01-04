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

import com.obj.nc.domain.BaseDynamicAttributesBean;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.content.TemplateWithModelContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.messageTemplating.config.ThymeleafConfiguration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BaseTemplateFormatter<TEMPLATE_TYPE extends TemplateWithModelContent<?>, OUTPUT_CONTENT_TYPE extends MessageContent> extends ProcessorFunctionAdapter<Message<TEMPLATE_TYPE>, List<Message<OUTPUT_CONTENT_TYPE>>> {
	
	public final static String LOCALE_ATTR_NAME = "@locale";
	
	private TemplateEngine templateEngine;
	
	private ThymeleafConfiguration config;

	@Override
	public Optional<PayloadValidationException> checkPreCondition(Message<TEMPLATE_TYPE> message) {
		TemplateWithModelContent<?> content = message.getBody();

		if (content ==null ) {
			return Optional.of(new PayloadValidationException("BaseTemplateFormatter cannot format message because its content is null"));
		}
		
		if (!(content instanceof  TemplateWithModelContent)) {
			return Optional.of(new PayloadValidationException("BaseTemplateFormatter cannot format message because its content is not of type TemplateWithModelContent. Instead is " +  content.getClass().getSimpleName()));
		}		

		return Optional.empty();
	}


	@Override
	public List<Message<OUTPUT_CONTENT_TYPE>> execute(Message<TEMPLATE_TYPE> payload) {
		List<Message<OUTPUT_CONTENT_TYPE>> result = new ArrayList<>();
		
		TemplateWithModelContent<?> emailFromTemplate = payload.getBody();
		
		List<Locale> forLocales = CollectionUtils.isEmpty(emailFromTemplate.getRequiredLocales())?
				config.getDefaultLocales()
				:
				emailFromTemplate.getRequiredLocales();
		
		for (Locale locale: forLocales) {
			Context ctx = new Context(locale);
			if (emailFromTemplate.getModel() instanceof BaseDynamicAttributesBean) {
				ctx.setVariables(((BaseDynamicAttributesBean)emailFromTemplate.getModel()).getAttributes());
			} else {
				ctx.setVariable("model", emailFromTemplate.getModel());
			}
			
			final String formattedContent = this.templateEngine.process(emailFromTemplate.getTemplateFileName(), ctx);
			
			Message<OUTPUT_CONTENT_TYPE> htmlMessage = createMessageWithFormattedContent(formattedContent, locale, payload);
			
			htmlMessage.setAttributeValue(LOCALE_ATTR_NAME,locale);
//			htmlMessage.setDeliveryOptions(payload.getDeliveryOptions());
			htmlMessage.setReceivingEndpoints(payload.getReceivingEndpoints());
			
			result.add(htmlMessage);
		}
		
		return result;
	}


	protected abstract Message<OUTPUT_CONTENT_TYPE> createMessageWithFormattedContent(String formattedContent, Locale locale,  Message<TEMPLATE_TYPE> payload);


}
