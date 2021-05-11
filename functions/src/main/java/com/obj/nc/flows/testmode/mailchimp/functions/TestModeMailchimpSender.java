package com.obj.nc.flows.testmode.mailchimp.functions;

import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.senders.MailchimpSender;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfigProperties;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderProcessorFunction;
import com.obj.nc.functions.processors.senders.mailchimp.model.MailchimpRenderTemplateRequest;
import com.obj.nc.functions.processors.senders.mailchimp.model.MailchimpSendTemplateRequest;
import com.obj.nc.functions.processors.senders.mailchimp.model.MailchimpRenderTemplateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfig.MAILCHIMP_REST_TEMPLATE;

@RequiredArgsConstructor
public class TestModeMailchimpSender extends ProcessorFunctionAdapter<Message,Message> implements MailchimpSender {
    
    public static final String RENDER_TEMPLATE_PATH = "/templates/render";
    public static final String RENDERED_MAILCHIMP_MESSAGE_ATTR_NAME = "RENDERED_MAILCHIMP_MESSAGE";
    
    @Qualifier(MAILCHIMP_REST_TEMPLATE) 
    @Autowired private RestTemplate restTemplate;
    @Autowired private MailchimpSenderConfigProperties mailchimpSenderConfigProperties;
    
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
        payload.<MailchimpContent>getContentTyped().getMessage().setTo(realMailchimpSender.mapRecipient(payload.getBody().getRecievingEndpoints().get(0)));
    
        MailchimpSendTemplateRequest sendReq = MailchimpSendTemplateRequest.from(payload.getContentTyped(), mailchimpSenderConfigProperties.getAuthKey());
        MailchimpRenderTemplateRequest renderReq = new MailchimpRenderTemplateRequest();
        renderReq.setKey(sendReq.getKey());
        renderReq.setTemplateName(sendReq.getTemplateName());
        renderReq.setTemplateContent(sendReq.getTemplateContent());
        renderReq.setMergeVars(sendReq.getMessage().getGlobalMergeVars());
        
        String renderedTemplateHtml = sendRenderRequest(renderReq);
        payload.getBody().getMessage().setAttributeValue(RENDERED_MAILCHIMP_MESSAGE_ATTR_NAME, renderedTemplateHtml);
        receiver.recieve(payload);
        return payload;
    }
    
    public String sendRenderRequest(MailchimpRenderTemplateRequest contentDto) {
        ResponseEntity<MailchimpRenderTemplateResponse> responseEntity = restTemplate.postForEntity(RENDER_TEMPLATE_PATH, contentDto, MailchimpRenderTemplateResponse.class);
        
        if (responseEntity.getBody() == null) {
            throw new RestClientException("Response body must not be null");
        }
        
        return responseEntity.getBody().getHtml();
    }
    
}
