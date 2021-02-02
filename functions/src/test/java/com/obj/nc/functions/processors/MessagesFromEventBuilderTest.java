package com.obj.nc.functions.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdExecution;
import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdPreCondition;
import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromEventExecution;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromEventPreCondition;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromEventProcessingFunction;
import org.junit.jupiter.api.Test;

import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.Body;
import com.obj.nc.domain.Header;
import com.obj.nc.domain.ProcessingInfo;
import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.DeliveryOptions.AGGREGATION_TYPE;
import com.obj.nc.domain.endpoints.DeliveryOptions.TIME_CONSTRAINT_TYPE;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.message.Message;
import com.obj.nc.utils.JsonUtils;

class MessagesFromEventBuilderTest {

	@Test
	void createMessagesFromEvent() {
		//GIVEN
		String INPUT_JSON_FILE = "events/direct_message.json";
		Event event = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Event.class);

		ValidateAndGenerateEventIdProcessingFunction funciton = new ValidateAndGenerateEventIdProcessingFunction(
				new ValidateAndGenerateEventIdExecution(),
				new ValidateAndGenerateEventIdPreCondition());

		event = funciton.apply(event);
		
		//WHEN
		MessagesFromEventProcessingFunction function = new MessagesFromEventProcessingFunction(
				new MessagesFromEventExecution(),
				new MessagesFromEventPreCondition());

		List<Message> result = function.apply(event);
		
		//THEN
		assertThat(result.size()).isEqualTo(3);
		
		Message message = result.get(0);
		ProcessingInfo processingInfo = message.getProcessingInfo();
		assertThat(processingInfo.getStepName()).isEqualTo("CreateMessagesFromEvent");
		assertThat(processingInfo.getStepIndex()).isEqualTo(2);
		assertThat(processingInfo.getPrevProcessingId()).isEqualTo(event.getProcessingInfo().getProcessingId());
		assertThat(processingInfo.getTimeStampStart()).isBeforeOrEqualTo(processingInfo.getTimeStampFinish());
		
		Header header = message.getHeader();
		assertThat(header.getConfigurationName()).isEqualTo(event.getHeader().getConfigurationName());
		assertThat(header.getAttributes())
			.contains(
					entry("custom-proerty1", Arrays.asList("xx","yy")), 
					entry("custom-proerty2", "zz")
			);
		assertThat(header.getId()).isNotEqualTo(event.getHeader().getId());
		
		Body body = message.getBody();
		List<RecievingEndpoint> recievingEndpoints = message.getBody().getRecievingEndpoints();
		assertThat(recievingEndpoints.size()).isEqualTo(1);
		
		RecievingEndpoint recipient = recievingEndpoints.get(0);
		assertThat(recipient).extracting("email").isIn("john.doe@objectify.sk", "john.dudly@objectify.sk", "all@objectify.sk");
		
		
		assertThat(body.getMessage()).isEqualTo(event.getBody().getMessage());
		assertThat(body.getMessage().getSubject()).isEqualTo("Subject");
		assertThat(body.getMessage().getText()).isEqualTo("Text");
		assertThat(body.getAttachments().size()).isEqualTo(0);

	}
	
	@Test
	void createMessagesFromEventDeliveryOptions() {
		//GIVEN
		String INPUT_JSON_FILE = "events/delivery_options.json";
		Event event = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Event.class);

		ValidateAndGenerateEventIdProcessingFunction funciton = new ValidateAndGenerateEventIdProcessingFunction(
				new ValidateAndGenerateEventIdExecution(),
				new ValidateAndGenerateEventIdPreCondition());

		event = funciton.apply(event);
		
		//WHEN
		MessagesFromEventProcessingFunction function = new MessagesFromEventProcessingFunction(
				new MessagesFromEventExecution(),
				new MessagesFromEventPreCondition());

		List<Message> result = function.apply(event);
		
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
		Event event = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Event.class);

		ValidateAndGenerateEventIdProcessingFunction funciton = new ValidateAndGenerateEventIdProcessingFunction(
				new ValidateAndGenerateEventIdExecution(),
				new ValidateAndGenerateEventIdPreCondition());

		event = funciton.apply(event);
		
		//WHEN
		MessagesFromEventProcessingFunction function = new MessagesFromEventProcessingFunction(
				new MessagesFromEventExecution(),
				new MessagesFromEventPreCondition());

		List<Message> result = function.apply(event);
		
		//THEN
		Message deliveryNullMessage = findMessageWithEnpoint(result, "john.doe@objectify.sk");
		
		List<Attachement> attachements = deliveryNullMessage.getBody().getAttachments();
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
