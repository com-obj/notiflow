package com.obj.nc.flows.testmode.mailchimp.functions;

import java.util.Optional;

import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.senders.MailchimpSender;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfigProperties;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderProcessorFunction;
import com.obj.nc.functions.processors.senders.mailchimp.model.MailchimpSendTemplateRequest;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestModeMailchimpSender extends ProcessorFunctionAdapter<Message<MailchimpContent>,Message<MailchimpContent>> implements MailchimpSender {
    
    private final MailchimpSenderConfigProperties mailchimpSenderConfigProperties;
    private final InMemoryMailchimpSourceSupplier receiver;
    private final MailchimpSenderProcessorFunction realMailchimpSender;
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(Message<MailchimpContent> payload) {
        if (!(payload.getBody() instanceof MailchimpContent)) {
            throw new PayloadValidationException("TestModeMailchimpSender can only process MailchimpContent content. Was " + payload.getBody() );
        }
        return Optional.empty();
    }
    
    @Override
    protected Message<MailchimpContent> execute(Message<MailchimpContent> payload) {
        MailchimpContent content = payload.getBody();
        content.setRecipients(realMailchimpSender.mapRecipient(payload.getRecievingEndpoints().get(0)));
        
        MailchimpSendTemplateRequest sendRequest = MailchimpSendTemplateRequest.from(content, mailchimpSenderConfigProperties.getAuthTestKey());
        realMailchimpSender.doSendMessage(sendRequest);
        
        receiver.recieve(payload);
        return payload;
    }
    
}
