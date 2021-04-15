package com.obj.nc.flows.testmode.mailchimp.functions;

import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sources.SourceSupplierAdapter;

import java.util.LinkedList;
import java.util.Optional;

public class InMemoryMailchimpSourceSupplier extends SourceSupplierAdapter<Message> {
    
    public static final String ORIGINAL_RECIPIENTS_EMAIL_ATTR_NAME = "ORIGINAL_RECIPIENTS_EMAIL";
    public static final String ORIGINAL_RECIPIENTS_NAME_ATTR_NAME = "ORIGINAL_RECIPIENTS_NAME";
    
    private final LinkedList<Message> recieved = new LinkedList<Message>();
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(Message payload) {
        return Optional.empty();
    }
    
    @Override
    protected Message execute() {
        if (recieved.isEmpty()) {
            return null;
        }
        
        Message mailchimpMessage = recieved.getFirst();
        
        EmailEndpoint recipient = (EmailEndpoint) mailchimpMessage.getBody().getRecievingEndpoints().iterator().next();
        
        EmailContent content = mailchimpMessage.getContentTyped();
        content.setAttributeValue(ORIGINAL_RECIPIENTS_EMAIL_ATTR_NAME, recipient.getEmail());
        if (recipient.getRecipient()!=null) {
            content.setAttributeValue(ORIGINAL_RECIPIENTS_NAME_ATTR_NAME, recipient.getRecipient().getName());
        }
        
        recieved.removeFirst();
        
        return mailchimpMessage;
    }
    
    public void recieve(Message msg) {
        recieved.addLast(msg);
    }
    
}
