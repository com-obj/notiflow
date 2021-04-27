package com.obj.nc.flows.testmode.mailchimp.functions;

import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sources.SourceSupplierAdapter;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class InMemoryMailchimpSourceSupplier extends SourceSupplierAdapter<Message> {
    
    public static final String ORIGINAL_RECIPIENTS_EMAIL_ATTR_NAME = "ORIGINAL_RECIPIENTS_EMAIL";
    
    private final Queue<Message> received = new LinkedList<Message>();
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(Message payload) {
        return Optional.empty();
    }
    
    @Override
    protected Message execute() {
        if (received.isEmpty()) {
            return null;
        }
        
        Message mailchimpMessage = received.poll();
        MailchimpEndpoint recipient = (MailchimpEndpoint) mailchimpMessage.getBody().getRecievingEndpoints().iterator().next();
        
        MailchimpContent content = mailchimpMessage.getContentTyped();
        EmailContent emailContent = content.copyToEmailContent();
        emailContent.setAttributeValue(ORIGINAL_RECIPIENTS_EMAIL_ATTR_NAME, recipient.getEmail());
        mailchimpMessage.getBody().setMessage(emailContent);
        
        return mailchimpMessage;
    }
    
    public void recieve(Message msg) {
        received.add(msg);
    }
    
}
