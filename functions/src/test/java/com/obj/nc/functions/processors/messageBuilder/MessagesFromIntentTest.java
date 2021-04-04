package com.obj.nc.functions.processors.messageBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdExecution;
import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdPreCondition;
import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdProcessingFunction;
import org.junit.jupiter.api.Test;

import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.Body;
import com.obj.nc.domain.Header;
import com.obj.nc.domain.ProcessingInfo;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.DeliveryOptions.AGGREGATION_TYPE;
import com.obj.nc.domain.endpoints.DeliveryOptions.TIME_CONSTRAINT_TYPE;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdProcessingFunction;
import com.obj.nc.utils.JsonUtils;

class MessagesFromIntentTest {

	@Test
	void createMessagesFromEvent() {
		//GIVEN
		String INPUT_JSON_FILE = "events/direct_message.json";
		NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);

		ValidateAndGenerateEventIdProcessingFunction funciton = new ValidateAndGenerateEventIdProcessingFunction(
				new ValidateAndGenerateEventIdExecution(),
				new ValidateAndGenerateEventIdPreCondition());

		notificationIntent = funciton.apply(notificationIntent);
		
		//WHEN
		MessagesFromNotificationIntentProcessingFunction function = new MessagesFromNotificationIntentProcessingFunction(
				new MessagesFromNotificationIntentExecution(),
				new MessagesFromNotificationIntentPreCondition());

		List<Message> result = function.apply(notificationIntent);
		
		//THEN
		assertThat(result.size()).isEqualTo(3);
		
		Message message = result.get(0);
		ProcessingInfo processingInfo = message.getProcessingInfo();
		assertThat(processingInfo.getStepName()).isEqualTo("CreateMessagesFromEvent");
		assertThat(processingInfo.getStepIndex()).isEqualTo(2);
		assertThat(processingInfo.getPrevProcessingId()).isEqualTo(notificationIntent.getProcessingInfo().getProcessingId());
		assertThat(processingInfo.getTimeStampStart()).isBeforeOrEqualTo(processingInfo.getTimeStampFinish());
		
		Header header = message.getHeader();
		assertThat(header.getFlowId()).isEqualTo(notificationIntent.getHeader().getFlowId());
		assertThat(header.getAttributes())
			.contains(
					entry("custom-proerty1", Arrays.asList("xx","yy")), 
					entry("custom-proerty2", "zz")
			);
		assertThat(header.getId()).isNotEqualTo(notificationIntent.getHeader().getId());
		
		Body body = message.getBody();
		List<RecievingEndpoint> recievingEndpoints = message.getBody().getRecievingEndpoints();
		assertThat(recievingEndpoints.size()).isEqualTo(1);
		
		RecievingEndpoint recipient = recievingEndpoints.get(0);
		assertThat(recipient).extracting("email").isIn("john.doe@objectify.sk", "john.dudly@objectify.sk", "all@objectify.sk");
		
        EmailContent emailContent = body.getContentTyped();
		assertThat(body.getMessage()).isEqualTo(notificationIntent.getBody().getMessage());
		assertThat(emailContent.getSubject()).isEqualTo("Subject");
		assertThat(emailContent.getText()).isEqualTo("Text");
		assertThat(emailContent.getAttachments().size()).isEqualTo(0);

	}
	
	@Test
	void createMessagesFromEventDeliveryOptions() {
		//GIVEN
		String INPUT_JSON_FILE = "events/delivery_options.json";
		NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);

		ValidateAndGenerateEventIdProcessingFunction funciton = new ValidateAndGenerateEventIdProcessingFunction(
				new ValidateAndGenerateEventIdExecution(),
				new ValidateAndGenerateEventIdPreCondition());

		notificationIntent = funciton.apply(notificationIntent);
		
		//WHEN
		MessagesFromNotificationIntentProcessingFunction function = new MessagesFromNotificationIntentProcessingFunction(
				new MessagesFromNotificationIntentExecution(),
				new MessagesFromNotificationIntentPreCondition());

		List<Message> result = function.apply(notificationIntent);
		
		//THEN
		assertThat(result.size()).isEqualTo(2);
		
		Message deliveryNullMessage = findMessageWithEnpoint(result, "john.doe@objectify.sk");
		
		DeliveryOptions msgDeliveryOptions =deliveryNullMessage.getBody().getDeliveryOptions();
		assertThat(msgDeliveryOptions).isNotNull();
		assertThat(msgDeliveryOptions.getAggregationType()).isEqualTo(AGGREGATION_TYPE.NONE);
		assertThat(msgDeliveryOptions.getSchedulingType()).isEqualTo(TIME_CONSTRAINT_TYPE.IMMEDIATE);
		
		RecievingEndpoint recipient = deliveryNullMessage.getBody().getRecievingEndpoints().get(0);
		assertThat(recipient.getDeliveryOptions()).isNull(); //v tomto case su uz delivery options na message a nie specificky pri recipientovi
		
		Message deliverySet = findMessageWithEnpoint(result, "john.dudly@objectify.sk");
		
		DeliveryOptions msgDeliveryOptions2 =deliverySet.getBody().getDeliveryOptions();
		assertThat(msgDeliveryOptions2).isNotNull();
		assertThat(msgDeliveryOptions2.getAggregationType()).isEqualTo(AGGREGATION_TYPE.ONCE_A_WEEK);
		assertThat(msgDeliveryOptions2.getSchedulingType()).isEqualTo(TIME_CONSTRAINT_TYPE.IMMEDIATE);
		
		recipient = deliverySet.getBody().getRecievingEndpoints().get(0);
		assertThat(recipient.getDeliveryOptions()).isNull(); //v tomto case su uz delivery options na message a nie specificky pri recipientovi

	}
	
	@Test
	void createMessagesFromEventAttachements() {
		//GIVEN
		String INPUT_JSON_FILE = "events/direct_message_attachements.json";
		NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);

		ValidateAndGenerateEventIdProcessingFunction funciton = new ValidateAndGenerateEventIdProcessingFunction(
				new ValidateAndGenerateEventIdExecution(),
				new ValidateAndGenerateEventIdPreCondition());

		notificationIntent = funciton.apply(notificationIntent);
		
		//WHEN
		MessagesFromNotificationIntentProcessingFunction function = new MessagesFromNotificationIntentProcessingFunction(
				new MessagesFromNotificationIntentExecution(),
				new MessagesFromNotificationIntentPreCondition());

		List<Message> result = function.apply(notificationIntent);
		
		//THEN
		Message deliveryNullMessage = findMessageWithEnpoint(result, "john.doe@objectify.sk");
		
        EmailContent emailContent = deliveryNullMessage.getContentTyped();
		List<Attachement> attachements = emailContent.getAttachments();
		assertThat(attachements).isNotNull();
		assertThat(attachements.size()).isEqualTo(2);
		assertThat(attachements).first().extracting("name").isEqualTo("name.extension");
		assertThat(attachements).first().extracting("fileURI").hasToString("http://domain/location/name.extension");
	}
	
	private Message findMessageWithEnpoint(List<Message> result, String endpointName) {
		Message deliveryNullMessage = result
				.stream()
				.filter( msg -> msg.getBody().getRecievingEndpoints().get(0).getEndpointId().equals(endpointName))
				.collect(Collectors.toList())
				.get(0);
		
		return deliveryNullMessage;
	}

}
