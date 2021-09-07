package com.obj.nc.domain.message;

import java.util.List;

import org.springframework.http.MediaType;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.Person;

import lombok.Data;

@Data
public class SendEmailMessageRequest {
    private String subject;
    private String text;
    private String html;
    private List<Recipient> to;
    
    @Data
    static class Recipient {
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