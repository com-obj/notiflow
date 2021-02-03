package com.obj.nc.functions.processors.messageAggregator;

import com.obj.nc.domain.message.Message;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Arrays;
import java.util.List;

@ActiveProfiles("junit-test")
@SpringJUnitConfig(classes = MessageAggregatorTestConfig.class)
class MessageAggregatorTest {

    @Autowired
    private MessageAggregatorProcessingFunction aggregateMessages;

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
        Assertions.assertThat(outputMessage.getBody()).isEqualTo(expected.getBody());
    }

}