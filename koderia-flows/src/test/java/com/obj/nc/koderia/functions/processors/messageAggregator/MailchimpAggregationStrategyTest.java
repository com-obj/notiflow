package com.obj.nc.koderia.functions.processors.messageAggregator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.content.mailchimp.AggregatedMailchimpData;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.content.mailchimp.MailchimpMergeVariable;
import com.obj.nc.domain.message.MailChimpMessage;
import com.obj.nc.functions.processors.messageAggregator.MessageAggregator;
import com.obj.nc.functions.processors.messageAggregator.aggregations.MailchimpMessageAggregationStrategy;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfigProperties;
import com.obj.nc.koderia.config.DomainConfig;
import com.obj.nc.koderia.domain.eventData.BaseKoderiaData;
import com.obj.nc.koderia.mapper.KoderiaMergeVarMapperImpl;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@JsonTest
@ContextConfiguration(classes = MailchimpAggregationStrategyTest.MailchimpAggregationStrategyTestConfig.class)
public class MailchimpAggregationStrategyTest {
    
    @Autowired private MailchimpMessageAggregationStrategy aggregateMailchimpMessages;
    @Autowired private MailchimpSenderConfigProperties properties;
    @Autowired private KoderiaMergeVarMapperImpl mergeVarMapper;
    
    @Test
    void testAggregateValidMessagesPass() {
        // given
    	MailChimpMessage message1 = JsonUtils.readObjectFromClassPathResource("mailchimp/aggregation/input_message_1.json", MailChimpMessage.class);
        MailchimpContent message1Content = message1.getBody();
        message1Content.setGlobalMergeVariables(mergeVarMapper.map(message1Content.getOriginalEvent()));
        MailChimpMessage message2 = JsonUtils.readObjectFromClassPathResource("mailchimp/aggregation/input_message_2.json", MailChimpMessage.class);
        MailchimpContent message2Content = message2.getBody();
        message2Content.setGlobalMergeVariables(mergeVarMapper.map(message2Content.getOriginalEvent()));
    
        // when
        MailChimpMessage outputMessage = (MailChimpMessage) aggregateMailchimpMessages.apply(Arrays.asList(message1, message2));
    
        // then
        MailchimpContent content = outputMessage.getBody();
        assertThat(content.getTemplateName(), equalTo(properties.getAggregatedMessageTemplateName()));
        assertThat(content.getTemplateContent(), empty());
        assertThat(content.getRecipients(), hasSize(1));
        assertThat(content.getRecipients().get(0).getEmail(), equalTo("john.doe@objectify.sk"));
        assertThat(content.getOriginalEvent(), nullValue());
        assertThat(content.getSubject(), equalTo(properties.getAggregatedMessageSubject()));
        
        for (MailchimpMergeVariable globalMergeVariable : content.getGlobalMergeVariables()) {
            if (message1Content.getOriginalEvent().getType().equals(globalMergeVariable.getName())) {
                AggregatedMailchimpData aggregatedContent = (AggregatedMailchimpData) globalMergeVariable.getContent();
                assertThat(aggregatedContent.getData(), hasSize(2));
                assertThat(aggregatedContent.getData(), containsInAnyOrder(message1Content.getOriginalEvent().<BaseKoderiaData>getData().asMailchimpMergeVarContent(),
                        message2Content.getOriginalEvent().<BaseKoderiaData>getData().asMailchimpMergeVarContent()));
            }
        }
    }
    
    @TestConfiguration
    @Import({
            MailchimpSenderConfigProperties.class,
            KoderiaMergeVarMapperImpl.class,
            DomainConfig.class
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
