package com.obj.nc.flows.testmode.mailchimp.functions;

import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sources.SourceSupplierAdapter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

@RequiredArgsConstructor
public class InMemoryMailchimpSourceSupplier extends SourceSupplierAdapter<Message> {
    
    private final Queue<Message> received = new LinkedList<>();
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(Message payload) {
        return Optional.empty();
    }
    
    @Override
    protected Message execute() {
        if (received.isEmpty()) {
            return null;
        }
    
        Message originalMessage = received.poll();
        MailchimpContent originalContent = originalMessage.getContentTyped();
    
        Message message = new Message();
        MailchimpContent content = new MailchimpContent();
        content.setSubject(originalContent.getSubject());
        content.setRecipients(new ArrayList<>(originalContent.getRecipients()));
        content.setSenderName(originalContent.getSenderName());
        content.setSenderEmail(originalContent.getSenderEmail());
        message.getBody().setMessage(content);
        return message;
    }
    
    public void recieve(Message msg) {
        received.add(msg);
    }
    
}
