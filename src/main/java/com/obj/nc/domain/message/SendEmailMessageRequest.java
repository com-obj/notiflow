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

package com.obj.nc.domain.message;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.Person;

import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailMessageRequest {
    private String subject;
    private String text;
    private String html;
    private List<Recipient> to;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recipient {
        private String email;
        private String name;
        
        public EmailEndpoint toReceivingEndpoint() {
            return EmailEndpoint.createForPerson(new Person(name), email);
        }
    }
    
    public EmailMessage toEmailMessage() {
        EmailMessage message = new EmailMessage();
        message.getBody().setSubject(subject);
        
        String messageText = html == null ? text : html;
        message.getBody().setText(messageText);
        
        String messageContentType = html == null ? MediaType.TEXT_PLAIN_VALUE : MediaType.TEXT_HTML_VALUE;
        message.getBody().setContentType(messageContentType);
        
        to.stream()
                .map(Recipient::toReceivingEndpoint)
                .forEach(message.getReceivingEndpoints()::add);
        return message;
    }
}