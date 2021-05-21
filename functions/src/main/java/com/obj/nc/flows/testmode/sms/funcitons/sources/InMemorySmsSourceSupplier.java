package com.obj.nc.flows.testmode.sms.funcitons.sources;

import java.util.LinkedList;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sources.SourceSupplierAdapter;

@Component
@ConditionalOnMissingBean(type = "SmsSender")
public class InMemorySmsSourceSupplier extends SourceSupplierAdapter<Message<SimpleTextContent>> {
	
	public static final String ORIGINAL_RECIPIENTS_PHONE_ATTR_NAME = "ORIGINAL_RECIPIENTS_PHONE_NUM";
	public static final String ORIGINAL_RECIPIENTS_NAME_ATTR_NAME = "ORIGINAL_RECIPIENTS_NAME";

	
	private LinkedList<Message<SimpleTextContent>> recieved = new LinkedList<Message<SimpleTextContent>>();

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(Message<SimpleTextContent> payload) {
		return Optional.empty();
	}

	@Override
	protected Message<SimpleTextContent> execute() {
		if (recieved.isEmpty()) {
			return null;
		}
		
		Message<SimpleTextContent> sms= recieved.getFirst();
		
		SimpleTextContent content =sms.getBody();
		
		SmsEndpoint recipient = content.getRecievingEndpoints().iterator().next();		

		content.setAttributeValue(ORIGINAL_RECIPIENTS_PHONE_ATTR_NAME, recipient.getPhone());
		if (recipient.getRecipient()!=null) {
			content.setAttributeValue(ORIGINAL_RECIPIENTS_NAME_ATTR_NAME, recipient.getRecipient().getName());
		}
		
		recieved.removeFirst();
		
		return sms;
	}
	
	public void recieve(Message<SimpleTextContent> msg) {
		recieved.addLast(msg);
	}
	
	public int getReceivedCount() {
		return recieved.size();
	}
	
	public void purgeAllReceivedMessages() {
		recieved = new LinkedList<>();
	}

}
