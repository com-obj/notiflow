package com.obj.nc.functions.processors.senders.mailchimp;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpMessageDto;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpSendRequestDto;
import org.springframework.stereotype.Component;

import static com.obj.nc.functions.processors.senders.mailchimp.config.MailchimpSenderConfig.SEND_PATH;

@Component
@DocumentProcessingInfo
public class MailchimpSender extends BaseMailchimpSender<MailchimpContent> {
    
    public MailchimpSender() {
        super(SEND_PATH);
    }
    
    @Override
    public MailchimpSendRequestDto createSendRequestBody(Message<MailchimpContent> payload) {
        MailchimpMessageDto messageDto = MailchimpMessageDto.builder()
                .subject(payload.getBody().getSubject())
                .html(payload.getBody().getHtml())
                .fromEmail(getProperties().getSenderEmail())
                .recipients(mapRecipientsToDto(payload.getReceivingEndpoints()))
                .attachments(mapAttachmentsToDto(payload.getBody().getAttachments()))
                .build();
        
        return MailchimpSendRequestDto.builder()
                .key(getProperties().getAuthKey())
                .message(messageDto)
                .build();
    }
    
}
