package com.obj.nc.functions.processors.messageAggregator;

import com.obj.nc.domain.message.Message;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class MessageAggregatorTest {

    private final MessageAggregatorProcessingFunction aggregateMessages = new MessageAggregatorProcessingFunction(
            new MessageAggregatorExecution(),
            new MessageAggregatorPreCondition());

    @Test
    void testAggregateMessages() {
        // given
        List<Message> inputMessages = Arrays.asList(
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message1.json", Message.class),
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message2.json", Message.class),
                JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_input_message3.json", Message.class)
        );

        // when
        Message outputMessage = aggregateMessages.apply(inputMessages);
        System.out.println(JsonUtils.writeObjectToJSONStringPretty(outputMessage));

        // then
        Message expected = JsonUtils.readObjectFromClassPathResource("messages/aggregate/aggregate_output_message.json", Message.class);
        Assertions.assertThat(outputMessage).isEqualTo(expected);
    }

}