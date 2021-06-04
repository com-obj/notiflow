package com.obj.nc.functions.processors.messageBuilder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.MailChimpMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.SimpleTextMessage;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.domain.notifIntent.content.IntentContent;
import com.obj.nc.functions.processors.eventIdGenerator.GenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpAttachmentDto;
import com.obj.nc.utils.JsonUtils;

class MessagesFromIntentTest {


	@SuppressWarnings("unchecked")
	@Test
	void createMessagesFromEvent() {
		//GIVEN
		String INPUT_JSON_FILE = "intents/direct_message.json";
		NotificationIntent<IntentContent> notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);

		GenerateEventIdProcessingFunction generateEventfunction = new GenerateEventIdProcessingFunction();
		notificationIntent = (NotificationIntent<IntentContent>)generateEventfunction.apply(notificationIntent);
		
		//WHEN
		MessagesFromNotificationIntentProcessingFunction createMessagesFunction = new MessagesFromNotificationIntentProcessingFunction();
		List<EmailMessage> result = (List<EmailMessage>)createMessagesFunction.apply(notificationIntent);
		
		//THEN
		assertThat(result.size()).isEqualTo(3);
		
		Message<EmailContent> message = result.get(0);
		
		List<? extends RecievingEndpoint> recievingEndpoints = message.getRecievingEndpoints();
		assertThat(recievingEndpoints.size()).isEqualTo(1);
		
		RecievingEndpoint recipient = recievingEndpoints.get(0);
		assertThat(recipient).extracting("email").isIn("john.doe@objectify.sk", "john.dudly@objectify.sk", "all@objectify.sk");
		
        EmailContent emailContent = message.getBody();
		assertThat(emailContent.getSubject()).isEqualTo("Subject");
		assertThat(emailContent.getText()).isEqualTo("Text");
		assertThat(emailContent.getAttachments().size()).isEqualTo(0);

	}
	
//	@Test
//	void createMessagesFromEventDeliveryOptions() {
//		//GIVEN
//		String INPUT_JSON_FILE = "events/delivery_options.json";
//		NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
//
//		GenerateEventIdProcessingFunction funciton = new GenerateEventIdProcessingFunction();
//
//		notificationIntent = (NotificationIntent)funciton.apply(notificationIntent);
//		
//		//WHEN
//		MessagesFromNotificationIntentProcessingFunction createMessagesFunction = new MessagesFromNotificationIntentProcessingFunction();
//		List<Message> result = createMessagesFunction.apply(notificationIntent);
//		
//		//THEN
//		assertThat(result.size()).isEqualTo(2);
//		
//		Message deliveryNullMessage = findMessageWithEnpoint(result, "john.doe@objectify.sk");
//		
//		DeliveryOptions msgDeliveryOptions =deliveryNullMessage.getBody().getDeliveryOptions();
//		assertThat(msgDeliveryOptions).isNotNull();
//		assertThat(msgDeliveryOptions.getAggregationType()).isEqualTo(AGGREGATION_TYPE.NONE);
//		assertThat(msgDeliveryOptions.getSchedulingType()).isEqualTo(TIME_CONSTRAINT_TYPE.IMMEDIATE);
//		
//		RecievingEndpoint recipient = deliveryNullMessage.getBody().getRecievingEndpoints().get(0);
//		assertThat(recipient.getDeliveryOptions()).isNull(); //v tomto case su uz delivery options na message a nie specificky pri recipientovi
//		
//		Message deliverySet = findMessageWithEnpoint(result, "john.dudly@objectify.sk");
//		
//		DeliveryOptions msgDeliveryOptions2 =deliverySet.getBody().getDeliveryOptions();
//		assertThat(msgDeliveryOptions2).isNotNull();
//		assertThat(msgDeliveryOptions2.getAggregationType()).isEqualTo(AGGREGATION_TYPE.ONCE_A_WEEK);
//		assertThat(msgDeliveryOptions2.getSchedulingType()).isEqualTo(TIME_CONSTRAINT_TYPE.IMMEDIATE);
//		
//		recipient = deliverySet.getBody().getRecievingEndpoints().get(0);
//		assertThat(recipient.getDeliveryOptions()).isNull(); //v tomto case su uz delivery options na message a nie specificky pri recipientovi
//
//	}
	
	@Test
	void createMessagesFromEventAttachements() {
		//GIVEN
		String INPUT_JSON_FILE = "intents/direct_message_attachements.json";
		NotificationIntent<IntentContent> notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);

		GenerateEventIdProcessingFunction generateEventFunc = new GenerateEventIdProcessingFunction();
		notificationIntent = (NotificationIntent<IntentContent>)generateEventFunc.apply(notificationIntent);
		
		//WHEN
		MessagesFromNotificationIntentProcessingFunction createMessagesFunction = new MessagesFromNotificationIntentProcessingFunction();
		List<Message<?>> result = (List<Message<?>>)createMessagesFunction.apply(notificationIntent);
		
		//THEN
		EmailMessage deliveryNullMessage = findMessageWithEnpoint(result, "john.doe@objectify.sk");
		
        EmailContent emailContent = deliveryNullMessage.getBody();
		List<Attachement> attachements = emailContent.getAttachments();
		assertThat(attachements).isNotNull();
		assertThat(attachements.size()).isEqualTo(2);
		assertThat(attachements).first().extracting("name").isEqualTo("name.extension");
		assertThat(attachements).first().extracting("fileURI").hasToString("http://domain/location/name.extension");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	void createMessagesFromEventDifferentChannels() {
		//GIVEN
		String INPUT_JSON_FILE = "intents/direct_message_different_channels.json";
		NotificationIntent<IntentContent> notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
		
		//WHEN
		MessagesFromNotificationIntentProcessingFunction createMessagesFunction = new MessagesFromNotificationIntentProcessingFunction();
		List<Message<?>> result = (List<Message<?>>)createMessagesFunction.apply(notificationIntent);
		
		//AND THEN
		assertThat(result.size()).isEqualTo(3);
		
		SimpleTextMessage message = findMessageWithEnpoint(result, "0918186997");
		
		List<? extends RecievingEndpoint> recievingEndpoints = message.getRecievingEndpoints();
		assertThat(recievingEndpoints.size()).isEqualTo(1);
				
		SimpleTextContent smsContent = message.getBody();
		assertThat(smsContent.getText()).isEqualTo("Text");
		
		//AND THEN
		MailChimpMessage mailChimpMessage = findMessageWithEnpoint(result, "all@objectify.sk");
		
        MailchimpContent emailContent = mailChimpMessage.getBody();
		List<MailchimpAttachmentDto> attachements = emailContent.getAttachments();
		assertThat(attachements).isNotNull();
		assertThat(attachements.size()).isEqualTo(2);
		assertThat(attachements).first().extracting("name").isEqualTo("name.extension");
		assertThat(attachements).first().extracting("content").isNotNull();

	}
	
	@SuppressWarnings("unchecked")
	private <T extends Message<?>> T findMessageWithEnpoint(List<Message<?>> result, String endpointName) {
		T deliveryNullMessage = (T) result
				.stream()
				.filter( msg -> msg.getRecievingEndpoints().get(0).getEndpointId().equals(endpointName))
				.collect(Collectors.toList())
				.get(0);
		
		return deliveryNullMessage;
	}

}
