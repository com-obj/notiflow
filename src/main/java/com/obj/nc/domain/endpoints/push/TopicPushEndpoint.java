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

package com.obj.nc.domain.endpoints.push;

import com.google.firebase.messaging.Message;
import com.obj.nc.domain.dto.endpoint.ReceivingEndpointDto;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false, of = "topic")
@Builder
public class TopicPushEndpoint extends PushEndpoint {
    
    public static final String JSON_TYPE_IDENTIFIER = "TOPIC_PUSH";
    
    private String topic;
    
    @Override
    public Message.Builder toFcmMessageBuilder() {
        return Message
                .builder()
                .setTopic(topic);
    }
    
    @Override
    public String getEndpointId() {
        return getTopic();
    }
    
    @Override
    public void setEndpointId(String endpointId) {
        setTopic(endpointId);
    }
    
    @Override
    public String getEndpointType() {
        return JSON_TYPE_IDENTIFIER;
    }

    @Override
    public ReceivingEndpointDto toDto() {
        return null;
    }

}
