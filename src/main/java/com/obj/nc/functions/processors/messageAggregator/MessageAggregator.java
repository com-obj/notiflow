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

package com.obj.nc.functions.processors.messageAggregator;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.messageAggregator.aggregations.BasePayloadAggregationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.integration.aggregator.AbstractAggregatingMessageGroupProcessor;
import org.springframework.integration.store.MessageGroup;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
public class MessageAggregator extends AbstractAggregatingMessageGroupProcessor {
    
    private final BasePayloadAggregationStrategy aggregationStrategy;
    
    @Override
    protected Object aggregatePayloads(MessageGroup group, Map<String, Object> defaultHeaders) {
        log.info("Starting aggregating message group of " +  group.getSequenceSize() + " messages");
        
        List<Message<?>> messages = group.getMessages().stream().map(sm -> ((Message<?>)sm.getPayload())).collect(Collectors.toList());
        
        
        return aggregationStrategy.apply(messages);
    }
}
