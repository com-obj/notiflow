package com.obj.nc.components;

import java.util.List;

import org.springframework.stereotype.Component;

import com.obj.nc.components.api.MailchimpContentFactory;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.content.mailchimp.MailchimpData;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpMergeVarMapper;
import com.obj.nc.functions.processors.senders.mailchimp.config.MailchimpSenderConfigProperties;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpAttachmentDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MailchimpContentFactoryImpl implements MailchimpContentFactory {
    
    private final MailchimpSenderConfigProperties mailchimpSenderConfigProperties;
    private final MailchimpMergeVarMapper mailchimpMergeVarMapper;

    public MailchimpContent createFromData(MailchimpData event) {
        MailchimpContent content = new MailchimpContent();
        content.setOriginalEvent(event);
        content.setSubject(mapSubject(event));
        content.setSenderName(mailchimpSenderConfigProperties.getSenderName());
        content.setSenderEmail(mailchimpSenderConfigProperties.getSenderEmail());
        content.setTemplateName(mailchimpSenderConfigProperties.getTemplateNameFromMessageType(event.getType()));
        content.setGlobalMergeVariables(mailchimpMergeVarMapper.map(event));
        content.setMergeLanguage(mailchimpSenderConfigProperties.getMergeLanguage());
        
        List<MailchimpAttachmentDto> mailchimpAttachmentDtos = MailchimpAttachmentDto.fromAttachements(event.getAttachments());
        content.setAttachments(mailchimpAttachmentDtos);
        return content;
    }

    protected String mapSubject(MailchimpData event) {
        return event.getSubject();
    }


}
