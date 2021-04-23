package com.obj.nc.flows.testmode.sms.funcitons.sources;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sources.SourceSupplierAdapter;
import org.springframework.stereotype.Component;

@Component
public class InMemorySmsSourceSupplier extends SourceSupplierAdapter<Message> {
	
	public static final String ORIGINAL_RECIPIENTS_PHONE_ATTR_NAME = "ORIGINAL_RECIPIENTS_PHONE_NUM";
	public static final String ORIGINAL_RECIPIENTS_NAME_ATTR_NAME = "ORIGINAL_RECIPIENTS_NAME";

	
	private LinkedList<Message> recieved = new LinkedList<Message>();

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(Message payload) {
		return Optional.empty();
	}

	@Override
	protected Message execute() {
		if (recieved.isEmpty()) {
			return null;
		}
		
		Message sms= recieved.getFirst();
		
		SmsEndpoint recipient = (SmsEndpoint)sms.getBody().getRecievingEndpoints().iterator().next();
		
		SimpleTextContent content =sms.getContentTyped();
		content.setAttributeValue(ORIGINAL_RECIPIENTS_PHONE_ATTR_NAME, recipient.getPhone());
		if (recipient.getRecipient()!=null) {
			content.setAttributeValue(ORIGINAL_RECIPIENTS_NAME_ATTR_NAME, recipient.getRecipient().getName());
		}
		
		recieved.removeFirst();
		
		return sms;
	}
	
	public void recieve(Message msg) {
		recieved.addLast(msg);
	}
	
	public int getReceivedCount() {
		return recieved.size();
	}
	
	public void purgeAllReceivedMessages() {
		recieved = new LinkedList<>();
	}

}
