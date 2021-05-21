package com.obj.nc.flows.testmode.mailchimp.functions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sources.SourceSupplierAdapter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InMemoryMailchimpSourceSupplier extends SourceSupplierAdapter<Message<MailchimpContent>> {
    
    private final Queue<Message<MailchimpContent>> received = new LinkedList<>();
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(Message<MailchimpContent> payload) {
        return Optional.empty();
    }
    
    @Override
    protected Message<MailchimpContent> execute() {
        if (received.isEmpty()) {
            return null;
        }
    
        Message<MailchimpContent> originalMessage = received.poll();
        MailchimpContent originalContent = originalMessage.getBody();
    
        //Why do I need to clone? Why not using the received originalContent;
        Message<MailchimpContent> message = new Message<MailchimpContent>();
        MailchimpContent content = new MailchimpContent();
        content.setSubject(originalContent.getSubject());
        content.setRecipients(new ArrayList<>(originalContent.getRecipients()));
        content.setSenderName(originalContent.getSenderName());
        content.setSenderEmail(originalContent.getSenderEmail());
        message.setBody(content);
        return message;
    }
    
    public void recieve(Message<MailchimpContent> msg) {
        received.add(msg);
    }
    
}
