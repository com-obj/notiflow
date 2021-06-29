package com.obj.nc.functions.processors.messageTracking;

import com.obj.nc.config.NcAppConfigProperties;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailReadTrackingDecorator extends ProcessorFunctionAdapter<Message<EmailContent>, Message<EmailContent>> {
    
    private final NcAppConfigProperties ncAppConfigProperties;
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(Message<EmailContent> payload) {
        Optional<PayloadValidationException> exception = super.checkPreCondition(payload);
        if (exception.isPresent()) {
            return exception;
        }
        if (payload.getBody() == null) {
            return Optional.of(new PayloadValidationException("Payload must not have null content"));
        }
        if (!MediaType.TEXT_HTML_VALUE.equals(payload.getBody().getContentType())) {
            return Optional.of(new PayloadValidationException(String.format("EmailReadTrackingDecorator can only decorate HTML text. Got: %s", payload.getBody().getContentType())));
        }
        if (payload.getBody().getText() == null) {
            return Optional.of(new PayloadValidationException("Payload must not have null content text"));
        }
        return Optional.empty();
    }
    
    @Override
    protected Message<EmailContent> execute(Message<EmailContent> payload) {
        EmailContent content = payload.getBody();
        String emailText = content.getText();
    
        Document html = Jsoup.parse(emailText);
        Element img = html.body().appendElement("img");
        
        URI readMessageCallbackUri = UriComponentsBuilder.fromHttpUrl(ncAppConfigProperties.getUrl())
                .path("/delivery-info/messages/read/{messageId}").build(payload.getMessageId());
        
        img.attr("src", readMessageCallbackUri.toString());
    
        emailText = html.html();
        content.setText(emailText);
        
        return payload;
    }
    
}