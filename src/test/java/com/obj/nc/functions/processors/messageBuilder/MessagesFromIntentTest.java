package com.obj.nc.functions.processors.messageBuilder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import com.obj.nc.domain.Attachment;
import org.junit.jupiter.api.Test;

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
import com.obj.nc.functions.processors.messageTeamplating.domain.TestModel;
import com.obj.nc.utils.JsonUtils;

class MessagesFromIntentTest {


	@SuppressWarnings("unchecked")
	@Test
	void createMessagesFromEvent() {
		//GIVEN
		String INPUT_JSON_FILE = "intents/direct_message.json";
		NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
		
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
	void createMessagesFromEventAttachements() {
		//GIVEN
		String INPUT_JSON_FILE = "intents/direct_message_attachements.json";
		NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
		
		//WHEN
		MessagesFromIntentGenerator createMessagesFunction = new MessagesFromIntentGenerator();
		List<Message<?>> result = (List<Message<?>>)createMessagesFunction.apply(notificationIntent);
		
		//THEN
		EmailMessage deliveryNullMessage = findMessageWithEnpoint(result, "john.doe@objectify.sk");
		
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
		String INPUT_JSON_FILE = "intents/direct_message_different_channels.json";
		NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
		
		//WHEN
		MessagesFromIntentGenerator createMessagesFunction = new MessagesFromIntentGenerator();
		List<Message<?>> result = (List<Message<?>>)createMessagesFunction.apply(notificationIntent);
		
		//AND THEN
		assertThat(result.size()).isEqualTo(2);
		
		SmsMessage message = findMessageWithEnpoint(result, "0918186997");
		
		List<? extends ReceivingEndpoint> receivingEndpoints = message.getReceivingEndpoints();
		assertThat(receivingEndpoints.size()).isEqualTo(1);
				
		SimpleTextContent smsContent = message.getBody();
		assertThat(smsContent.getText()).isEqualTo("Text");
		
		//AND THEN
		EmailMessage mail = findMessageWithEnpoint(result, "john.doe@objectify.sk");
		
        EmailContent emailContent = mail.getBody();
		List<Attachment> attachments = emailContent.getAttachments();
		assertThat(attachments).isNotNull();
		assertThat(attachments.size()).isEqualTo(2);
		assertThat(attachments).first().extracting("name").isEqualTo("name.extension");
		assertThat(attachments).first().extracting("filePathAndName").isEqualTo("src/test/resources/intents/0_ba_job_post.json");
	}

	@SuppressWarnings("unchecked")
	@Test
	void createTemplatedMessagesFromEventDifferentChannels() {
		//GIVEN
		String INPUT_JSON_FILE = "intents/teamplate_message_en_de.json";
		NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
		
		//WHEN
		MessagesFromIntentGenerator createMessagesFunction = new MessagesFromIntentGenerator();
		List<Message<?>> result = (List<Message<?>>)createMessagesFunction.apply(notificationIntent);
		
		//AND THEN
		assertThat(result.size()).isEqualTo(2);
		
		SmsMessageTemplated<TestModel> message = findMessageWithEnpoint(result, "0908186997");
		
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
		EmailMessageTemplated<TestModel> mail = findMessageWithEnpoint(result, "john.doe@objectify.sk");
		
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
		assertThat(attachments).first().extracting("filePathAndName").isEqualTo("src/test/resources/intents/0_ba_job_post.json");
	}
	
	
	
	@SuppressWarnings("unchecked")
	private <T extends Message<?>> T findMessageWithEnpoint(List<Message<?>> result, String endpointName) {
		T deliveryNullMessage = (T) result
				.stream()
				.filter( msg -> msg.getReceivingEndpoints().get(0).getEndpointId().equals(endpointName))
				.collect(Collectors.toList())
				.get(0);
		
		return deliveryNullMessage;
	}

}
