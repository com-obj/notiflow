package com.obj.nc.functions.processors.messageAggregator.aggregations;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.sms.AggregatedSmsContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.message.Message;

import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class SmsMessageAggregationStrategy extends StandardMessageAggregationStrategy {
	
	//TODO: Check all content of incomming messages is EmailContent

	@Override
	protected Message execute(List<Message> messages) {
		Message outputMessage = Message.createAsAggregatedSms();
		outputMessage.getBody().setRecievingEndpoints(messages.get(0).getBody().getRecievingEndpoints());
		outputMessage.getBody().setDeliveryOptions(messages.get(0).getBody().getDeliveryOptions());

		AggregatedSmsContent aggregatedContent = outputMessage.getContentTyped();
		messages.stream().map(BasePayload::<SimpleTextContent>getContentTyped).forEach(aggregatedContent::add);
		return outputMessage;
	}

}