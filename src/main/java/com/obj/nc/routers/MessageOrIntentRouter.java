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

package com.obj.nc.routers;

import com.obj.nc.Get;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import org.springframework.integration.router.AbstractMessageRouter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;

import static com.obj.nc.flows.intenProcessing.NotificationIntentProcessingFlowConfig.INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.messageProcessing.MessageProcessingFlowConfig.MESSAGE_PROCESSING_FLOW_INPUT_CHANNEL_ID;

@Component
public class MessageOrIntentRouter extends AbstractMessageRouter {
    
    @Override
    protected Collection<MessageChannel> determineTargetChannels(Message<?> message) {
        if (message.getPayload() instanceof com.obj.nc.domain.message.Message<?>) {
            MessageChannel destChannel = Get.getBean(MESSAGE_PROCESSING_FLOW_INPUT_CHANNEL_ID, MessageChannel.class);
            return Arrays.asList(destChannel);
        }
    
        if (message.getPayload() instanceof NotificationIntent) {
            MessageChannel destChannel = Get.getBean(INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID, MessageChannel.class);
            return Arrays.asList(destChannel);
        }
    
        throw new RuntimeException("Cannot route any other type than Message or Intent in InputEventExtensionConvertingFlowConfig");
    }
    
}
