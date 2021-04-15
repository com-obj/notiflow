package com.obj.nc.flows.testmode.mailchimp.functions;

import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.senders.MailchimpSender;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class TestModeMailchimpSender extends ProcessorFunctionAdapter<Message,Message> implements MailchimpSender {
    
    private final InMemoryMailchimpSourceSupplier receiver;
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(Message payload) {
        if (!(payload.getBody().getMessage() instanceof EmailContent)) {
            throw new PayloadValidationException("TestModeMailchimpSender can only process EmailContent content. Was " + payload.getBody().getMessage() );
        }
        return Optional.empty();
    }
    
    @Override
    protected Message execute(Message payload) {
        receiver.recieve(payload);
        return payload;
    }
    
}
