package com.obj.nc.flows.testmode.mailchimp.functions;

import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.senders.MailchimpSender;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfigProperties;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderProcessorFunction;
import com.obj.nc.functions.processors.senders.mailchimp.model.MailchimpSendTemplateRequest;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class TestModeMailchimpSender extends ProcessorFunctionAdapter<Message,Message> implements MailchimpSender {
    
    private final MailchimpSenderConfigProperties mailchimpSenderConfigProperties;
    private final InMemoryMailchimpSourceSupplier receiver;
    private final MailchimpSenderProcessorFunction realMailchimpSender;
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(Message payload) {
        if (!(payload.getBody().getMessage() instanceof MailchimpContent)) {
            throw new PayloadValidationException("TestModeMailchimpSender can only process MailchimpContent content. Was " + payload.getBody().getMessage() );
        }
        return Optional.empty();
    }
    
    @Override
    protected Message execute(Message payload) {
        MailchimpContent content = payload.getContentTyped();
        content.setRecipients(realMailchimpSender.mapRecipient(payload.getBody().getRecievingEndpoints().get(0)));
        
        MailchimpSendTemplateRequest sendRequest = MailchimpSendTemplateRequest.from(content, mailchimpSenderConfigProperties.getAuthTestKey());
        realMailchimpSender.doSendMessage(sendRequest);
        
        receiver.recieve(payload);
        return payload;
    }
    
}
