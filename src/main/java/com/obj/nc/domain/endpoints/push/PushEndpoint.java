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
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public abstract class PushEndpoint extends ReceivingEndpoint {
    
    public abstract Message.Builder toFcmMessageBuilder();
    
    public static PushEndpoint ofTopic(String topic) {
        return TopicPushEndpoint
                .builder()
                .topic(topic)
                .build();
    }
    
    public static PushEndpoint ofToken(String token) {
        return DirectPushEndpoint
                .builder()
                .token(token)
                .build();
    }
    
}
