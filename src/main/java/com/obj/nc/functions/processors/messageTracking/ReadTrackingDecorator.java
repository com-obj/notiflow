package com.obj.nc.functions.processors.messageTracking;

import java.net.URI;
import java.util.Optional;

import com.obj.nc.domain.content.TrackableContent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.obj.nc.config.NcAppConfigProperties;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReadTrackingDecorator extends ProcessorFunctionAdapter<Message<TrackableContent>, Message<TrackableContent>> {
    
    private final NcAppConfigProperties ncAppConfigProperties;
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(Message<TrackableContent> payload) {
        Optional<PayloadValidationException> exception = super.checkPreCondition(payload);
        if (exception.isPresent()) {
            return exception;
        }
        
        TrackableContent body = payload.getBody();
        if (body == null) {
            return Optional.of(new PayloadValidationException("Payload must not have null content"));
        }
        if (!body.hasHtmlText()) {
            return Optional.of(new PayloadValidationException("ReadTrackingDecorator can only decorate HTML text."));
        }
        return Optional.empty();
    }
    
    @Override
    protected Message<TrackableContent> execute(Message<TrackableContent> payload) {
        Message<TrackableContent> result = Message.newTypedMessageFrom(payload.getClass(), payload);
        result.setReceivingEndpoints(payload.getReceivingEndpoints());
        result.setAttributes(payload.getAttributes());
        result.setBody(payload.getBody());
    
        TrackableContent content = result.getBody();
        String emailText = content.getHtmlText();
    
        Document html = Jsoup.parse(emailText);
        Element img = html.body().appendElement("img");
        
        URI readMessageCallbackUri = UriComponentsBuilder
                .fromHttpUrl(ncAppConfigProperties.getUrl())
                .path("/delivery-info/messages/{messageId}/mark-as-read")
                .build(result.getId());
        
        img.attr("src", readMessageCallbackUri.toString());
    
        emailText = html.html();
        content.setHtmlText(emailText);
        
        return result;
    }
    
}
