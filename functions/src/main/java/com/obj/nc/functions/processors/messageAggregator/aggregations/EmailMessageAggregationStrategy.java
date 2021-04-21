package com.obj.nc.functions.processors.messageAggregator.aggregations;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.content.email.AggregatedEmailContent;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.message.Message;

import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class EmailMessageAggregationStrategy extends StandardMessageAggregationStrategy {

	@Override
	protected Message execute(List<Message> messages) {
		Message outputMessage = Message.createAsAggregatedEmail();
		outputMessage.getBody().setRecievingEndpoints(messages.get(0).getBody().getRecievingEndpoints());
		outputMessage.getBody().setDeliveryOptions(messages.get(0).getBody().getDeliveryOptions());

		AggregatedEmailContent aggregatedContent = outputMessage.getContentTyped();
		messages.stream().map(BasePayload::<EmailContent>getContentTyped).forEach(aggregatedContent::add);
		return outputMessage;
	}

}