package com.obj.nc.flows.testmode.mailchimp.functions;

import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfig;
import com.obj.nc.functions.processors.senders.mailchimp.model.MailchimpContentDto;
import com.obj.nc.functions.sources.SourceSupplierAdapter;
import com.obj.nc.utils.JsonUtils;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

@RequiredArgsConstructor
public class InMemoryMailchimpSourceSupplier extends SourceSupplierAdapter<Message> {
    
    public static final String ORIGINAL_RECIPIENTS_EMAIL_ATTR_NAME = "ORIGINAL_RECIPIENTS_EMAIL";
    public static final String MAILCHIMP_MESSAGE_AS_JSON_ATTR_NAME = "MAILCHIMP_MESSAGE_AS_JSON";
    
    private final Queue<MailchimpContentDto> received = new LinkedList<>();
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(Message payload) {
        return Optional.empty();
    }
    
    @Override
    protected Message execute() {
        if (received.isEmpty()) {
            return null;
        }
        
        MailchimpContentDto mailchimpMessage = received.poll();
        
        Message message = new Message();
        
        MailchimpContent content = new MailchimpContent();
        content.setMessage(mailchimpMessage.getMessage());
        content.setAttributeValue(ORIGINAL_RECIPIENTS_EMAIL_ATTR_NAME, mailchimpMessage.getMessage().getTo().get(0));
        content.setAttributeValue(MAILCHIMP_MESSAGE_AS_JSON_ATTR_NAME, JsonUtils.writeObjectToJSONStringPretty(mailchimpMessage));
        
        message.getBody().setMessage(content);
        return message;
    }
    
    public void recieve(MailchimpContentDto msg) {
        received.add(msg);
    }
    
}
