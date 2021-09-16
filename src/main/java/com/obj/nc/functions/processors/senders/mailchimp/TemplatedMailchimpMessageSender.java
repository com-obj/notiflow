/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
