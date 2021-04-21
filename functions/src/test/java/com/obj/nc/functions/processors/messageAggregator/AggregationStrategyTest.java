package com.obj.nc.functions.processors.messageAggregator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
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
        Assertions.assertThatThrownBy(() -> aggregateEmailMessages.apply(inputMessages))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("There are no input messages to process");
    }

    @Test
    void testAggregateMessageAggregationTypeNoneFail() {
        // given
        List<Message> inputMessages = Arrays.asList(
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message1.json", Message.class),
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message1_none.json", Message.class)
        );

        // when - then
        Assertions.assertThatThrownBy(() -> aggregateEmailMessages.apply(inputMessages))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Input message is not intended to be aggregated");
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
                .hasMessageContaining("Messages do not share the same delivery options");
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
                .hasMessageContaining("Messages do not share the same receiving endpoints");
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