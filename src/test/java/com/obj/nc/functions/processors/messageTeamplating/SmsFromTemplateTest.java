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

package com.obj.nc.functions.processors.messageTeamplating;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.message.SmsMessage;
import com.obj.nc.domain.message.SmsMessageTemplated;
import com.obj.nc.functions.processors.messageTemplating.EmailTemplateFormatter;
import com.obj.nc.functions.processors.messageTemplating.SmsTemplateFormatter;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
class SmsFromTemplateTest extends BaseIntegrationTest {
	
	@Autowired private SmsTemplateFormatter template2Sms;

	@Test
	void createSimpleHtmlEmailFromTemplate() {
		//GIVEN
		String INPUT_JSON_FILE = "messages/templated/txt_template_message.json";
		SmsMessageTemplated msg = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, SmsMessageTemplated.class);
		
		//WHEN
		List<SmsMessage> htmlMessages = template2Sms.apply(msg);
		
		//THEN
		assertThat(htmlMessages.size()).isEqualTo(1);
		
		SimpleTextContent content = htmlMessages.iterator().next().getBody();
		
		System.out.println(content.getText());
		assertThat(content.getText()).contains("part1", "part2", "John Doe");
	}
	
	@Test
	void createHtmlEmailFromPojoModelAndTemplate() {
		//GIVEN
		String INPUT_JSON_FILE = "messages/templated/txt_template_message_pojo_model.json";
		SmsMessageTemplated msg = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, SmsMessageTemplated.class);
		
		//WHEN
		List<SmsMessage> htmlMessages = template2Sms.apply(msg);
		
		//THEN
		assertThat(htmlMessages.size()).isEqualTo(1);
		
		SimpleTextContent content = htmlMessages.iterator().next().getBody();
		
		System.out.println(content.getText());
		assertThat(content.getText()).contains("val11, val12", "val21, val22", "John Doe");
		assertThat(content.getText()).doesNotContain("No parts available");
	}
	
	@Test
	void createI18NHtmlEmailFromTemplate() {
		//GIVEN
		String INPUT_JSON_FILE = "messages/templated/txt_template_message_en_de.json";
		SmsMessageTemplated msg = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, SmsMessageTemplated.class);
		
		//WHEN
		List<SmsMessage> htmlMessages = template2Sms.apply(msg);
		
		//THEN
		assertThat(htmlMessages.size()).isEqualTo(2);
		
		SimpleTextContent deContent = htmlMessages
				.stream()
				.filter(m-> Locale.GERMAN.equals(m.getAttributes().get(EmailTemplateFormatter.LOCALE_ATTR_NAME)))
				.findFirst()
				.get().getBody();
		
		System.out.println(deContent.getText());
		assertThat(deContent.getText()).contains("Grues gott");
		assertThat(deContent.getText()).contains("John Doe");
		
		SimpleTextContent enContent = htmlMessages
				.stream()
				.filter(m-> Locale.US.equals(m.getAttributes().get(EmailTemplateFormatter.LOCALE_ATTR_NAME)))
				.findFirst()
				.get().getBody();
		
		System.out.println(enContent.getText());
		assertThat(enContent.getText()).contains("Hallo World");
		assertThat(deContent.getText()).contains("John Doe");
	}

}
