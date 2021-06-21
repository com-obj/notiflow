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

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.messageAggregator.AggregationStrategyTest.MessageAggregatorTestConfig;
import com.obj.nc.functions.processors.messageAggregator.aggregations.EmailMessageAggregationStrategy;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringJUnitConfig(classes = MessageAggregatorTestConfig.class)
class AggregationStrategyTest {

    @Autowired private EmailMessageAggregationStrategy aggregateEmailMessages;
    
    @Test
    void testAggregateValidMessagesPass() {
        // given
        List<Message> inputMessages = Arrays.asList(
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message1.json", Message.class),
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message2.json", Message.class),
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message3.json", Message.class)
        );

        // when
        Message outputMessage = (Message) aggregateEmailMessages.apply(inputMessages);

        // then
        Message expected = JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_output_message.json", Message.class);
        Assertions.assertThat(outputMessage.getBody()).isEqualTo(expected.getBody());
    }

    @Test
    void testAggregateEmptyMessageListFail() {
        // given
        List<Message> inputMessages = new ArrayList<>();
        // when - then
        Object outputMessage = aggregateEmailMessages.apply(inputMessages);
        // then
        assertThat(outputMessage, nullValue());
    }

    @Test
    @Disabled
    void testAggregateMessageAggregationTypeNoneFail() {
        // given
        List<Message> inputMessages = Arrays.asList(
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message1_none.json", Message.class),
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message1.json", Message.class)
        );

        // when - then
        Assertions.assertThatThrownBy(() -> aggregateEmailMessages.apply(inputMessages))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("has invalid aggregation type")
                .hasMessageContaining("NONE");
    }

    @Test
    void testAggregateMessagesWithDifferentDeliveryOptionsFail() {
        // given
        List<Message> inputMessages = Arrays.asList(
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message1.json", Message.class),
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message1_delivery_options.json", Message.class)
        );

        // when - then
        Assertions.assertThatThrownBy(() -> aggregateEmailMessages.apply(inputMessages))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("has different delivery options to other payloads")
                .hasMessageContaining("17:00");
    }

    @Test
    void testAggregateMessagesWithDifferentReceivingEndpointsFail() {
        // given
        List<Message> inputMessages = Arrays.asList(
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message1.json", Message.class),
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message1_receiving_endpoints.json", Message.class)
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