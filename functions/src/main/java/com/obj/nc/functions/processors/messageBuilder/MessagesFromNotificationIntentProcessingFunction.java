package com.obj.nc.functions.processors.messageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.Body;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
@DocumentProcessingInfo("GenerateMessagesFromEvent")
public class MessagesFromNotificationIntentProcessingFunction extends ProcessorFunctionAdapter<NotificationIntent, List<Message>> {

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(NotificationIntent notificationIntent) {

		if (notificationIntent.getBody().getRecievingEndpoints().isEmpty()) {
			return Optional.of(new PayloadValidationException(
					String.format("NotificationIntent %s has no receiving endpoints defined.", notificationIntent)));
		}

		return Optional.empty();
	}

	@Override
	protected List<Message> execute(NotificationIntent notificationIntent) {
		log.debug("Create messages for {}",  notificationIntent);

		List<Message> messages = new ArrayList<Message>();

		for (RecievingEndpoint recievingEndpoint: notificationIntent.getBody().getRecievingEndpoints()) {

			Message msg = new Message();
			
			Body msgBody = msg.getBody();
			Body eventBody = notificationIntent.getBody();
			msgBody.addRecievingEndpoints(recievingEndpoint);

			if (recievingEndpoint.getDeliveryOptions()!=null) {
				msgBody.setDeliveryOptions(recievingEndpoint.getDeliveryOptions());
				recievingEndpoint.setDeliveryOptions(null);
			} else {
				msgBody.setDeliveryOptions(eventBody.getDeliveryOptions());
			}

			msgBody.setAttributes(eventBody.getAttributes());
			
			msgBody.setMessage(eventBody.getMessage());
			msgBody.getMessage().setAttributes(eventBody.getMessage().getAttributes());

			messages.add(msg);
		}

		return messages;
	}

}
