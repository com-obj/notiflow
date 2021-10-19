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

package com.obj.nc.functions.processors.senders;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.push.PushContent;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.push.PushEndpoint;
import com.obj.nc.domain.message.PushMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
@DocumentProcessingInfo
public class PushSender extends ProcessorFunctionAdapter<PushMessage, PushMessage> {
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(PushMessage payload) {
        Optional<PayloadValidationException> exception = super.checkPreCondition(payload);
        if (exception.isPresent()) {
            return exception;
        }
    
        List<? extends ReceivingEndpoint> endpoints = payload.getReceivingEndpoints();
    
        if (endpoints.size() != 1) {
            return Optional.of(new PayloadValidationException("PushSender can send to only one recipient. Found more: " + endpoints));
        }
    
        ReceivingEndpoint endpoint = endpoints.get(0);
        if (!(endpoint instanceof PushEndpoint)) {
            return Optional.of(new PayloadValidationException("PushSender can send to PushEndpoint endpoints only. Found " + endpoint));
        }
        
        return Optional.empty();
    }
    
    @Override
    protected PushMessage execute(PushMessage payload) {
        PushContent content = payload.getBody();
        
        Message firebaseMessage = payload
                .getReceivingEndpoints()
                .get(0)
                .toFcmMessageBuilder()
                .setNotification(
                        Notification
                                .builder()
                                .setTitle(content.getSubject())
                                .setBody(content.getText())
                                .setImage(content.getIconUrl())
                                .build()
                )
                .build();
    
        try {
            String fcmMessageId = FirebaseMessaging
                    .getInstance()
                    .send(firebaseMessage);
            
            payload.setAttributeValue("fcmMessageId", fcmMessageId);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(String.format("Error sending Push message : %s", e));
        }
        
        return payload;
    }
    
}
