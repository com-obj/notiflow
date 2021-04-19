package com.obj.nc.functions.processors.messageAggregator.aggregations;

import com.obj.nc.domain.Messages;
import com.obj.nc.domain.content.email.AggregatedEmailContent;
import com.obj.nc.domain.message.Message;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EmailMessageAggregationStrategy extends StandardMessageAggregationStrategy {

	@Override
	protected Message execute(Messages messages) {
		Message outputMessage = Message.createAsAggregatedEmail();
		outputMessage.getBody().setRecievingEndpoints(messages.getMessages().get(0).getBody().getRecievingEndpoints());
		outputMessage.getBody().setDeliveryOptions(messages.getMessages().get(0).getBody().getDeliveryOptions());

		AggregatedEmailContent aggregatedContent = outputMessage.getContentTyped();
		for (Message msg : messages.getMessages()) {
			aggregatedContent.add(msg.getContentTyped());
		}

		return outputMessage;
	}

}