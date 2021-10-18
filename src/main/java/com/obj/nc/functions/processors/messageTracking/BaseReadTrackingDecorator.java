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

package com.obj.nc.functions.processors.messageTracking;

import com.obj.nc.config.NcAppConfigProperties;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.content.TrackableContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

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
