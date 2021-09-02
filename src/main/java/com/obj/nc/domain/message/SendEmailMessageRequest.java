package com.obj.nc.domain.message;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.Person;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

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
                .forEach(message.getRecievingEndpoints()::add);
        return message;
    }
}