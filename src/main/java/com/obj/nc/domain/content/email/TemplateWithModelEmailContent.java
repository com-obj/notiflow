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

package com.obj.nc.domain.content.email;

import static com.obj.nc.functions.processors.messageTemplating.config.ThymeleafConfiguration.MESSAGE_SOURCE_FOR_TEMPLATES_BEAN_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.obj.nc.domain.Attachment;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import com.obj.nc.Get;
import com.obj.nc.domain.content.TemplateWithModelContent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Log4j2
@Builder
@AllArgsConstructor
public class TemplateWithModelEmailContent<MODEL_TYPE> extends TemplateWithModelContent<MODEL_TYPE>  {
	
	private String subjectResourceKey;
	
	private String[] subjectResourcesMessageParameters;
	
	private String subject;

	@Builder.Default
	private List<Attachment> attachments = new ArrayList<Attachment>();
		
	public String getSubjectLocalised(Locale locale) {		
		try {
			return Get.getBean(MESSAGE_SOURCE_FOR_TEMPLATES_BEAN_NAME, MessageSource.class).getMessage(getSubjectResourceKey(), getSubjectResourcesMessageParameters(), locale);
		} catch (NoSuchMessageException e) {
			log.debug("{} not found in resource bundle. Fallback to subject property", getSubjectResourceKey());
		}
		return subject;
	}
	
}
