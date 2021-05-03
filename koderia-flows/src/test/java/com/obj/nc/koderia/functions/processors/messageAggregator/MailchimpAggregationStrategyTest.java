package com.obj.nc.koderia.functions.processors.messageAggregator;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.messageAggregator.MessageAggregator;
import com.obj.nc.functions.processors.messageAggregator.aggregations.MailchimpMessageAggregationStrategy;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfigProperties;
import com.obj.nc.koderia.functions.processors.eventConverter.KoderiaEventConverterConfig;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@JsonTest
@ContextConfiguration(classes = MailchimpAggregationStrategyTest.MailchimpAggregationStrategyTestConfig.class)
public class MailchimpAggregationStrategyTest {
    
    @Autowired private MailchimpMessageAggregationStrategy aggregateEmailMessages;
    
    @Test
    void testAggregateValidMessagesPass() {
        // given
        List<Message> inputMessages = Arrays.asList(
                JsonUtils.readObjectFromClassPathResource("mailchimp/aggregation/input_message_1.json", Message.class),
                JsonUtils.readObjectFromClassPathResource("mailchimp/aggregation/input_message_2.json", Message.class),
                JsonUtils.readObjectFromClassPathResource("mailchimp/aggregation/input_message_3.json", Message.class)
        );
        
        // when
        Message outputMessage = (Message) aggregateEmailMessages.apply(inputMessages);
    
        // then
        Message expectedOutputMessage = JsonUtils.readObjectFromClassPathResource("mailchimp/aggregation/output_message.json", Message.class);
        Assertions.assertThat(outputMessage.getBody().getMessage()).isEqualTo(expectedOutputMessage.getBody().getMessage());
    }
    
    @TestConfiguration
    @Import({
            MailchimpSenderConfigProperties.class,
            KoderiaEventConverterConfig.class
    })
    public static class MailchimpAggregationStrategyTestConfig {
        @Autowired private MailchimpSenderConfigProperties properties;
        
        @Bean
        public MailchimpMessageAggregationStrategy mailchimpMessageAggregationStrategy() {
            return new MailchimpMessageAggregationStrategy(properties);
        }
        
        @Bean
        public MessageAggregator aggregateMailchimpMessages() {
            return new MessageAggregator(mailchimpMessageAggregationStrategy());
        }
    }
    
}
