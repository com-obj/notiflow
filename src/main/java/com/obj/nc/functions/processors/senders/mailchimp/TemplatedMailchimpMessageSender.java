package com.obj.nc.functions.processors.senders.mailchimp;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.mailchimp.TemplatedMailchimpContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpMergeVariableDto;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpMessageDto;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpSendRequestDto;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpTemplateContentDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.obj.nc.functions.processors.senders.mailchimp.config.MailchimpSenderConfig.SEND_TEMPLATE_PATH;

@Component
@DocumentProcessingInfo
public class TemplatedMailchimpMessageSender extends BaseMailchimpSender<TemplatedMailchimpContent> {
    
    public TemplatedMailchimpMessageSender() {
        super(SEND_TEMPLATE_PATH);
    }
    
    @Override
    public MailchimpSendRequestDto createSendRequestBody(Message<TemplatedMailchimpContent> payload) {
        MailchimpMessageDto messageDto = MailchimpMessageDto.builder()
                .subject(payload.getBody().getSubject())
                .fromEmail(getProperties().getSenderEmail())
                .recipients(mapRecipientsToDto(payload.getReceivingEndpoints()))
                .attachments(mapAttachmentsToDto(payload.getBody().getAttachments()))
                .globalMergeVars(mapMergeVariables(payload.getBody().getMergeVariables()))
                .mergeLanguage(payload.getBody().getMergeLanguage())
                .build();
        
        return MailchimpSendRequestDto.builder()
                .message(messageDto)
                .templateName(payload.getBody().getTemplateName())
                .templateContent(mapTemplateContent(payload.getBody().getTemplateContent()))
                .build();
    }
    
    private List<MailchimpMergeVariableDto> mapMergeVariables(Map<String, Object> mergeVariables) {
        List<MailchimpMergeVariableDto> result = new ArrayList<>();
        
        mergeVariables.forEach((key, value) -> result.add(
                MailchimpMergeVariableDto.builder()
                        .name(key)
                        .content(value)
                        .build())
        );
        
        return result;
    }
    
    private List<MailchimpTemplateContentDto> mapTemplateContent(Map<String, String> templateContent) {
        List<MailchimpTemplateContentDto> result = new ArrayList<>();
    
        templateContent.forEach((key, value) -> result.add(
                MailchimpTemplateContentDto.builder()
                        .name(key)
                        .content(value)
                        .build())
        );
        
        return result;
    }
    
}
