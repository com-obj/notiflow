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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.messageAggregator.AggregationStrategyTest.MessageAggregatorTestConfig;
import com.obj.nc.functions.processors.messageAggregator.aggregations.EmailMessageAggregationStrategy;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringJUnitConfig(classes = MessageAggregatorTestConfig.class)
class AggregationStrategyTest {

    @Autowired private EmailMessageAggregationStrategy aggregateEmailMessages;
    
    @Test
    void testAggregateValidMessagesPass() {
        // given
        List<Message<EmailContent>> inputMessages = Arrays.asList(
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message1.json", EmailMessage.class),
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message2.json", EmailMessage.class),
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message3.json", EmailMessage.class)
        );

        // when
        EmailMessage outputMessage = (EmailMessage) aggregateEmailMessages.apply(inputMessages);

        // then
        EmailMessage expected = JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_output_message.json", EmailMessage.class);
        Assertions.assertThat(outputMessage.getBody()).isEqualTo(expected.getBody());
    }

    @Test
    void testAggregateEmptyMessageListFail() {
        // given
        List<Message<EmailContent>> inputMessages = new ArrayList<>();
        // when - then
        Object outputMessage = aggregateEmailMessages.apply(inputMessages);
        // then
        assertThat(outputMessage, nullValue());
    }

    @Test
    @Disabled
    void testAggregateMessageAggregationTypeNoneFail() {
        // given
        List<Message<EmailContent>> inputMessages = Arrays.asList(
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message1_none.json", EmailMessage.class),
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message1.json", EmailMessage.class)
        );

        // when - then
        Assertions.assertThatThrownBy(() -> aggregateEmailMessages.apply(inputMessages))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("has invalid aggregation type")
                .hasMessageContaining("NONE");
    }

//    @Test
//    void testAggregateMessagesWithDifferentDeliveryOptionsFail() {
//        // given
//        List<Message<EmailContent>> inputMessages = Arrays.asList(
//                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message1.json", Message.class),
//                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message1_delivery_options.json", Message.class)
//        );
//
//        // when - then
//        Assertions.assertThatThrownBy(() -> aggregateEmailMessages.apply(inputMessages))
//                .isInstanceOf(PayloadValidationException.class)
//                .hasMessageContaining("has different delivery options to other payloads")
//                .hasMessageContaining("17:00");
//    }

    @Test
    void testAggregateMessagesWithDifferentReceivingEndpointsFail() {
        // given
        List<Message<EmailContent>> inputMessages = Arrays.asList(
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message1.json", EmailMessage.class),
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message1_receiving_endpoints.json", EmailMessage.class)
        );

        // when - then
        Assertions.assertThatThrownBy(() -> aggregateEmailMessages.apply(inputMessages))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("has different recipients to other payloads")
                .hasMessageContaining("john.doe@objectify.sk")
                .hasMessageContaining("john.dudly@objectify.sk");
    }
    
    @TestConfiguration
    public static class MessageAggregatorTestConfig {

        @Bean
        public EmailMessageAggregationStrategy emailMessageAggregationStrategy() {
            return new EmailMessageAggregationStrategy();
        }
    
        @Bean
        public MessageAggregator aggregateEmailMessages() {
            return new MessageAggregator(emailMessageAggregationStrategy());
        }
        
    }

}