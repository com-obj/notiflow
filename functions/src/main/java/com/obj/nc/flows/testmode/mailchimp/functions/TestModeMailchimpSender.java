package com.obj.nc.flows.testmode.mailchimp.functions;

import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.senders.MailchimpSender;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderProcessorFunction;
import com.obj.nc.functions.processors.senders.mailchimp.model.MailchimpContentDto;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class TestModeMailchimpSender extends ProcessorFunctionAdapter<Message,Message> implements MailchimpSender {
    
    private final InMemoryMailchimpSourceSupplier receiver;
    private final MailchimpSenderProcessorFunction realMailchimpSender;
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(Message payload) {
        if (!(payload.getBody().getMessage() instanceof MailchimpContent)) {
            throw new PayloadValidationException("TestModeMailchimpSender can only process EmailContent content. Was " + payload.getBody().getMessage() );
        }
        return Optional.empty();
    }
    
    @Override
    protected Message execute(Message payload) {
        MailchimpContentDto dto = MailchimpContentDto.from(payload.getContentTyped(), "HIDDEN");
        dto.getMessage().setTo(realMailchimpSender.mapRecipient(payload.getBody().getRecievingEndpoints().get(0)));
        receiver.recieve(dto);
        return payload;
    }
    
}
