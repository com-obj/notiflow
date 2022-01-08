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

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.obj.nc.domain.Attachment;
import com.obj.nc.domain.content.TemplateWithModelContent;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.EmailMessageTemplated;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.SmsMessage;
import com.obj.nc.domain.message.SmsMessageTemplated;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.domain.notifIntent.content.TemplatedIntentContent;
import com.obj.nc.functions.processors.messageTeamplating.domain.TestChildModel;
import com.obj.nc.functions.processors.messageTeamplating.domain.TestModel;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest(properties = {
    "nc.contacts-store.jsonStorePathAndFileName=src/test/resources/contact-store/contact-store.json", 
})
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
class MessagesFromIntentTest extends BaseIntegrationTest {

	@SuppressWarnings("unchecked")
	@Test
	void createMessagesFromEvent() {
		//GIVEN
        NotificationIntent notificationIntent = NotificationIntent.createWithStaticContent(
            "Subject", 
            "Text"
        );        
        notificationIntent.addRecipientsByName(
            "John Doe",
            "John Dudly",
            "Objectify"
        );

		//WHEN
		MessagesFromIntentGenerator createMessagesFunction = new MessagesFromIntentGenerator();
		List<EmailMessage> result = (List<EmailMessage>)createMessagesFunction.apply(notificationIntent);
		
		//THEN
		assertThat(result.size()).isEqualTo(3);
		
		Message<EmailContent> message = result.get(0);
		
		List<? extends ReceivingEndpoint> receivingEndpoints = message.getReceivingEndpoints();
		assertThat(receivingEndpoints.size()).isEqualTo(1);
		
		ReceivingEndpoint recipient = receivingEndpoints.get(0);
		assertThat(recipient).extracting("email").isIn("john.doe@objectify.sk", "john.dudly@objectify.sk", "all@objectify.sk");
		
        EmailContent emailContent = message.getBody();
		assertThat(emailContent.getSubject()).isEqualTo("Subject");
		assertThat(emailContent.getText()).isEqualTo("Text");
		assertThat(emailContent.getAttachments().size()).isEqualTo(0);

	}
	
	@Test
	void createMessagesFromEventAttachments() {
		//GIVEN
        NotificationIntent notificationIntent = NotificationIntent.createWithStaticContent(
            "Subject", 
            "Text",
			Attachment.builder().name("name.extension").fileURI(URI.create("http://domain/location/name.extension")).build(),
			Attachment.builder().name("name.extension").fileURI(URI.create("http://domain/location/name.extension")).build()
        );        
        notificationIntent.addRecipientsByName(
            "John Doe",
            "John Dudly"
        );

		//WHEN
		MessagesFromIntentGenerator createMessagesFunction = new MessagesFromIntentGenerator();
		List<Message<?>> result = (List<Message<?>>)createMessagesFunction.apply(notificationIntent);
		
		//THEN
		EmailMessage deliveryNullMessage = findMessageWithEndpoint(result, "john.doe@objectify.sk");
		
        EmailContent emailContent = deliveryNullMessage.getBody();
		List<Attachment> attachments = emailContent.getAttachments();
		assertThat(attachments).isNotNull();
		assertThat(attachments.size()).isEqualTo(2);
		assertThat(attachments).first().extracting("name").isEqualTo("name.extension");
		assertThat(attachments).first().extracting("fileURI").hasToString("http://domain/location/name.extension");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	void createMessagesFromEventDifferentChannels() {
		//GIVEN
        NotificationIntent notificationIntent = NotificationIntent.createWithStaticContent(
            "Subject", 
            "Text",
			Attachment.builder().name("name.extension").filePathAndName("someFile.tmp").build(),
			Attachment.builder().name("name.extension").filePathAndName("someFile.tmp").build()
        );        
        notificationIntent.addRecipientsByName(
            "John Doe",
            "Phone"
        );

		//WHEN
		MessagesFromIntentGenerator createMessagesFunction = new MessagesFromIntentGenerator();
		List<Message<?>> result = (List<Message<?>>)createMessagesFunction.apply(notificationIntent);
		
		//AND THEN
		assertThat(result.size()).isEqualTo(2);
		
		SmsMessage message = findMessageWithEndpoint(result, "+421905000111");
		
		List<? extends ReceivingEndpoint> receivingEndpoints = message.getReceivingEndpoints();
		assertThat(receivingEndpoints.size()).isEqualTo(1);
				
		SimpleTextContent smsContent = message.getBody();
		assertThat(smsContent.getText()).isEqualTo("Text");
		
		//AND THEN
		EmailMessage mail = findMessageWithEndpoint(result, "john.doe@objectify.sk");
		
        EmailContent emailContent = mail.getBody();
		List<Attachment> attachments = emailContent.getAttachments();
		assertThat(attachments).isNotNull();
		assertThat(attachments.size()).isEqualTo(2);
		assertThat(attachments).first().extracting("name").isEqualTo("name.extension");
		assertThat(attachments).first().extracting("filePathAndName").isEqualTo("someFile.tmp");
	}

	@SuppressWarnings("unchecked")
	@Test
	void createTemplatedMessagesFromEventDifferentChannels() {
		//GIVEN
		TemplatedIntentContent<Object> templatedContent = TemplatedIntentContent.builder()
			.templateFileName("test-template2.txt")
			.locale(new Locale("en_US"))
			.locale(new Locale("de"))
			.subjectResourceKey("some.subject.resource.key")
			.model(TestModel.builder()
				.name("John Doe")
				.part(TestChildModel.builder().field1("val11").field2("val12").build())
				.part(TestChildModel.builder().field1("val21").field2("val22").build())
				.build()
			).build();
		NotificationIntent notificationIntent = NotificationIntent.createWithTemplatedContent(
			templatedContent,	
			Attachment.builder().name("name.extension").filePathAndName("someFile.tmp").build(),
			Attachment.builder().name("name.extension").filePathAndName("someFile.tmp").build()
		);        
		notificationIntent.addRecipientsByName(
			"John Doe",
			"Phone"
		);
		
		//WHEN
		MessagesFromIntentGenerator createMessagesFunction = new MessagesFromIntentGenerator();
		List<Message<?>> result = (List<Message<?>>)createMessagesFunction.apply(notificationIntent);
		
		//AND THEN
		assertThat(result.size()).isEqualTo(2);
		
		SmsMessageTemplated<TestModel> message = findMessageWithEndpoint(result, "+421905000111");
		
		List<? extends ReceivingEndpoint> receivingEndpoints = message.getReceivingEndpoints();
		assertThat(receivingEndpoints.size()).isEqualTo(1);
				
		TemplateWithModelContent<TestModel> smsContent = message.getBody();
		assertThat(smsContent.getTemplateFileName()).isEqualTo("test-template2.txt");
//		assertThat(smsContent.getRequiredLocales()).isEqualTo(Arrays.asList(new Locale("en_US"), new Locale("de")));		
		assertThat(smsContent.getModel()).isNotNull();
		assertThat(smsContent.getModel().getClass()).isEqualTo(TestModel.class);
		
		TestModel model = smsContent.getModel();
		assertThat(model.getName()).isEqualTo("John Doe");
		
		//AND THEN
		EmailMessageTemplated<TestModel> mail = findMessageWithEndpoint(result, "john.doe@objectify.sk");
		
		TemplateWithModelEmailContent<TestModel> emailContent = mail.getBody();
		assertThat(smsContent.getTemplateFileName()).isEqualTo("test-template2.txt");
//		assertThat(smsContent.getRequiredLocales()).isEqualTo(Arrays.asList(new Locale("en_US"), new Locale("de")));		
		assertThat(smsContent.getModel()).isNotNull();
		assertThat(smsContent.getModel().getClass()).isEqualTo(TestModel.class);
		
		model = smsContent.getModel();
		assertThat(model.getName()).isEqualTo("John Doe");
		
		List<Attachment> attachments = emailContent.getAttachments();
		assertThat(attachments).isNotNull();
		assertThat(attachments.size()).isEqualTo(2);
		assertThat(attachments).first().extracting("name").isEqualTo("name.extension");
		assertThat(attachments).first().extracting("filePathAndName").isEqualTo("someFile.tmp");
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Message<?>> T findMessageWithEndpoint(List<Message<?>> result, String endpointName) {
		T deliveryNullMessage = (T) result
				.stream()
				.filter( msg -> msg.getReceivingEndpoints().get(0).getEndpointId().equals(endpointName))
				.collect(Collectors.toList())
				.get(0);
		
		return deliveryNullMessage;
	}

}
