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

package com.obj.nc.flows.testmode.email.functions.processors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode
@RequiredArgsConstructor
public class TestModeDigestModel {
	
	private List<EmailContent> emailContents = new ArrayList<>();
	private List<SimpleTextContent> smsContents = new ArrayList<>();

	public void addEmailContent(EmailContent emailContent) {
		emailContents.add(emailContent);
		if (!emailContent.getContentType().equals(MediaType.TEXT_HTML_VALUE)) {
			return;
		}
		
		String bodyPartHtml = StringUtils.substringBetween(emailContent.getText(), "<body>", "</body>");
		emailContent.setText("<div>" + bodyPartHtml + "</div>");
	}

	public void addSmsContent(SimpleTextContent smsContent) {
		smsContents.add(smsContent);
	}
	
}
