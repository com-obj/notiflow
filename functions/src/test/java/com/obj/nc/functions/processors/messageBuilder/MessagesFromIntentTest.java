package com.obj.nc.functions.processors.messageBuilder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.Body;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.processors.eventIdGenerator.GenerateEventIdProcessingFunction;
import com.obj.nc.utils.JsonUtils;

class MessagesFromIntentTest {


	@SuppressWarnings("unchecked")
	@Test
	void createMessagesFromEvent() {
		//GIVEN
		String INPUT_JSON_FILE = "events/direct_message.json";
		NotificationIntent<EmailContent> notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);

		GenerateEventIdProcessingFunction generateEventfunction = new GenerateEventIdProcessingFunction();
		notificationIntent = (NotificationIntent<EmailContent>)generateEventfunction.apply(notificationIntent);
		
		//WHEN
		MessagesFromNotificationIntentProcessingFunction<EmailContent> createMessagesFunction = new MessagesFromNotificationIntentProcessingFunction<EmailContent>();
		List<Message<EmailContent>> result = createMessagesFunction.apply(notificationIntent);
		
		//THEN
		assertThat(result.size()).isEqualTo(3);
		
		Message<EmailContent> message = result.get(0);
		
		List<? extends RecievingEndpoint> recievingEndpoints = message.getRecievingEndpoints();
		assertThat(recievingEndpoints.size()).isEqualTo(1);
		
		RecievingEndpoint recipient = recievingEndpoints.get(0);
		assertThat(recipient).extracting("email").isIn("john.doe@objectify.sk", "john.dudly@objectify.sk", "all@objectify.sk");
		
        EmailContent emailContent = message.getBody();
		assertThat(emailContent).isEqualTo(notificationIntent.getBody());
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
		String INPUT_JSON_FILE = "events/direct_message_attachements.json";
		NotificationIntent<EmailContent> notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);

		GenerateEventIdProcessingFunction funciton = new GenerateEventIdProcessingFunction();

		notificationIntent = (NotificationIntent<EmailContent>)funciton.apply(notificationIntent);
		
		//WHEN
		MessagesFromNotificationIntentProcessingFunction<EmailContent> createMessagesFunction = new MessagesFromNotificationIntentProcessingFunction<EmailContent>();
		List<Message<EmailContent>> result = createMessagesFunction.apply(notificationIntent);
		
		//THEN
		Message<EmailContent> deliveryNullMessage = findMessageWithEnpoint(result, "john.doe@objectify.sk");
		
        EmailContent emailContent = deliveryNullMessage.getBody();
		List<Attachement> attachements = emailContent.getAttachments();
		assertThat(attachements).isNotNull();
		assertThat(attachements.size()).isEqualTo(2);
		assertThat(attachements).first().extracting("name").isEqualTo("name.extension");
		assertThat(attachements).first().extracting("fileURI").hasToString("http://domain/location/name.extension");
	}
	
	private Message<EmailContent> findMessageWithEnpoint(List<Message<EmailContent>> result, String endpointName) {
		Message<EmailContent> deliveryNullMessage = result
				.stream()
				.filter( msg -> msg.getRecievingEndpoints().get(0).getEndpointId().equals(endpointName))
				.collect(Collectors.toList())
				.get(0);
		
		return deliveryNullMessage;
	}

}
