package com.obj.nc.flows.testmode.sms.funcitons.sources;

import java.util.LinkedList;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.SmstMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sources.SourceSupplierAdapter;

@Component
@ConditionalOnMissingBean(type = "SmsSender")
public class InMemorySmsSourceSupplier extends SourceSupplierAdapter<SmstMessage> {
	
	public static final String ORIGINAL_RECIPIENTS_PHONE_ATTR_NAME = "ORIGINAL_RECIPIENTS_PHONE_NUM";
	public static final String ORIGINAL_RECIPIENTS_NAME_ATTR_NAME = "ORIGINAL_RECIPIENTS_NAME";

	
	private LinkedList<SmstMessage> recieved = new LinkedList<SmstMessage>();

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(SmstMessage payload) {
		return Optional.empty();
	}

	@Override
	protected SmstMessage execute() {
		if (recieved.isEmpty()) {
			return null;
		}
		
		SmstMessage sms= recieved.getFirst();
		
		SimpleTextContent content =sms.getBody();
		
		SmsEndpoint recipient = (SmsEndpoint)sms.getRecievingEndpoints().iterator().next();		

		content.setAttributeValue(ORIGINAL_RECIPIENTS_PHONE_ATTR_NAME, recipient.getPhone());
		if (recipient.getRecipient()!=null) {
			content.setAttributeValue(ORIGINAL_RECIPIENTS_NAME_ATTR_NAME, recipient.getRecipient().getName());
		}
		
		recieved.removeFirst();
		
		return sms;
	}
	
	public void recieve(SmstMessage msg) {
		recieved.addLast(msg);
	}
	
	public int getReceivedCount() {
		return recieved.size();
	}
	
	public void purgeAllReceivedMessages() {
		recieved = new LinkedList<>();
	}

}
