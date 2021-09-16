/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
public class MailchimpMessageSender extends BaseMailchimpSender<MailchimpContent> {
    
    public MailchimpMessageSender() {
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
