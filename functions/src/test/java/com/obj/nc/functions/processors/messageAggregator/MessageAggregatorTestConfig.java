package com.obj.nc.functions.processors.messageAggregator;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MessageAggregatorTestConfig {

    @Bean
    public MessageAggregatorProcessingFunction aggregateMessages() {
        return new MessageAggregatorProcessingFunction(aggregateMessagesExecution(), aggregateMessagesPreCondition());
    }

    @Bean
    public MessageAggregatorExecution aggregateMessagesExecution() {
        return new MessageAggregatorExecution();
    }

    @Bean
    public MessageAggregatorPreCondition aggregateMessagesPreCondition() {
        return new MessageAggregatorPreCondition();
    }

}
