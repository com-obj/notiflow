package com.obj.nc.flows.testmode.mailchimp.functions;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.senders.mailchimp.model.MailchimpSendTemplateRequest;
import com.obj.nc.functions.sources.SourceSupplierAdapter;
import com.obj.nc.utils.JsonUtils;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import static com.obj.nc.flows.testmode.mailchimp.functions.TestModeMailchimpSender.RENDERED_MAILCHIMP_MESSAGE_ATTR_NAME;

@RequiredArgsConstructor
public class InMemoryMailchimpSourceSupplier extends SourceSupplierAdapter<Message> {
    
    public static final String ORIGINAL_RECIPIENTS_EMAIL_ATTR_NAME = "ORIGINAL_RECIPIENTS_EMAIL";
    
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
        content.setMessage(originalContent.getMessage());
        content.setAttributeValue(ORIGINAL_RECIPIENTS_EMAIL_ATTR_NAME, originalContent.getMessage().getTo().get(0));
        String html = (String) originalContent.getAttributeValue(RENDERED_MAILCHIMP_MESSAGE_ATTR_NAME);
        Handlebars handlebars = new Handlebars();
        try {
            Template template = handlebars.compileInline(html);
            String templatedHtml = template.apply(content.getMessage().getGlobalMergeVars());
            content.setAttributeValue(RENDERED_MAILCHIMP_MESSAGE_ATTR_NAME, templatedHtml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        message.getBody().setMessage(content);
        return message;
    }
    
    public void recieve(Message msg) {
        received.add(msg);
    }
    
}
