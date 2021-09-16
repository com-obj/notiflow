package com.obj.nc.functions.processors.messageTracking;

import java.net.URI;
import java.util.Optional;

import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.content.TrackableContent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.web.util.UriComponentsBuilder;

import com.obj.nc.config.NcAppConfigProperties;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseReadTrackingDecorator<T extends MessageContent> extends ProcessorFunctionAdapter<Message<T>, Message<T>> {
    
    private final NcAppConfigProperties ncAppConfigProperties;
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(Message<T> payload) {
        Optional<PayloadValidationException> exception = super.checkPreCondition(payload);
        if (exception.isPresent()) {
            return exception;
        }
        if (!(payload.getBody() instanceof TrackableContent)) {
            return Optional.of(new PayloadValidationException("Payload must implement TrackableContent"));
        }
        
        TrackableContent body = (TrackableContent) payload.getBody();
        if (body == null) {
            return Optional.of(new PayloadValidationException("Payload must not have null content"));
        }
        if (!body.hasHtmlText()) {
            return Optional.of(new PayloadValidationException("ReadTrackingDecorator can only decorate HTML text."));
        }
        return Optional.empty();
    }
    
    @Override
    protected Message<T> execute(Message<T> payload) {
        Message<T> result = Message.newTypedMessageFrom(payload.getClass(), payload);
        result.setReceivingEndpoints(payload.getReceivingEndpoints());
        result.setAttributes(payload.getAttributes());
        result.setBody(payload.getBody());
    
        TrackableContent content = (TrackableContent) result.getBody();
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