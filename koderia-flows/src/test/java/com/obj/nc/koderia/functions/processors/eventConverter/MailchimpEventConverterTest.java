package com.obj.nc.koderia.functions.processors.eventConverter;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.components.MailchimpContentFactoryImpl;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.content.mailchimp.MailchimpData;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.MailChimpMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.eventFactory.MailchimpEventConverter;
import com.obj.nc.functions.processors.senders.mailchimp.config.MailchimpSenderConfigProperties;
import com.obj.nc.koderia.config.DomainConfig;
import com.obj.nc.koderia.domain.event.BlogKoderiaEventDto;
import com.obj.nc.koderia.domain.event.EventKoderiaEventDto;
import com.obj.nc.koderia.domain.event.JobPostKoderiaEventDto;
import com.obj.nc.koderia.domain.event.LinkKoderiaEventDto;
import com.obj.nc.koderia.domain.event.NewsKoderiaEventDto;
import com.obj.nc.koderia.domain.eventData.BlogEventDataDto;
import com.obj.nc.koderia.domain.eventData.EventEventDataDto;
import com.obj.nc.koderia.domain.eventData.LinkEventDataDto;
import com.obj.nc.koderia.domain.eventData.NewsEventDataDto;
import com.obj.nc.koderia.mapper.KoderiaMergeVarMapperImpl;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@JsonTest(properties = {
        "nc.flows.input-evet-routing.type-propery-name=type"
})
@AutoConfigureWebClient
@ContextConfiguration(classes = {
        MailchimpEventConverter.class,
        KoderiaMergeVarMapperImpl.class,
        MailchimpContentFactoryImpl.class,
        DomainConfig.class,
        MailchimpSenderConfigProperties.class
})
class MailchimpEventConverterTest {

    @Autowired private MailchimpEventConverter eventConverter;

    @Test
    void testExtractJobPostDto() {
        // given
        JobPostKoderiaEventDto jobPostEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", JobPostKoderiaEventDto.class);
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(jobPostEvent));
    
        // when
        MailChimpMessage mappedNotificationIntent = eventConverter.apply(genericEvent);

        // then
        //file doens't exists
//        MatcherAssert.assertThat(mappedNotificationIntent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/job_post_event_schema.json"));

        // and
        MailchimpContent content = mappedNotificationIntent.getBody();
        MatcherAssert.assertThat(mappedNotificationIntent.getHeader().getFlowId(), equalTo("default-flow"));
        MatcherAssert.assertThat(content.getSubject(), equalTo(jobPostEvent.getSubject()));
        // and
        MailchimpData data = content.getOriginalEvent();
        MatcherAssert.assertThat(data, notNullValue());
    }

    @Test
    void testExtractNullEmitEventDto() {
        // when - then
        Assertions.assertThatThrownBy(() -> eventConverter.apply(null))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("GenericEvent must not be null");
    }

    @Test
    void testExtractBlogDto() {
        // given
        BlogKoderiaEventDto baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/blog_body.json", BlogKoderiaEventDto.class);
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEvent));

        // when
        MailChimpMessage mappedNotificationIntent = eventConverter.apply(genericEvent);

        // then
        BlogEventDataDto blogEventData = baseKoderiaEvent.getData();
        MailchimpContent content = mappedNotificationIntent.getBody();
        MatcherAssert.assertThat(mappedNotificationIntent.getHeader().getFlowId(), equalTo("default-flow"));
        MatcherAssert.assertThat(content.getSubject(), equalTo(blogEventData.getTitle()));
        // and
        MailchimpData data = content.getOriginalEvent();
        MatcherAssert.assertThat(data, notNullValue());
    }

    @Test
    void testExtractEventDto() {
        // given
        EventKoderiaEventDto baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/event_body.json", EventKoderiaEventDto.class);
    
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEvent));
        
        // when
        MailChimpMessage mappedNotificationIntent = eventConverter.apply(genericEvent);

        // then
        EventEventDataDto eventEventData = baseKoderiaEvent.getData();
        MailchimpContent content = mappedNotificationIntent.getBody();
        MatcherAssert.assertThat(mappedNotificationIntent.getHeader().getFlowId(), equalTo("default-flow"));
        MatcherAssert.assertThat(content.getSubject(), equalTo(eventEventData.getName()));
        // and
        MailchimpData data = content.getOriginalEvent();
        MatcherAssert.assertThat(data, notNullValue());
    }

    @Test
    void testExtractLinkDto() {
        // given
        LinkKoderiaEventDto baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/link_body.json", LinkKoderiaEventDto.class);
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEvent));

        // when
        MailChimpMessage mappedNotificationIntent = eventConverter.apply(genericEvent);

        // then
        LinkEventDataDto linkEventData = baseKoderiaEvent.getData();
        MailchimpContent content = mappedNotificationIntent.getBody();
        MatcherAssert.assertThat(mappedNotificationIntent.getHeader().getFlowId(), equalTo("default-flow"));
        MatcherAssert.assertThat(content.getSubject(), equalTo(linkEventData.getTitle()));
        // and
        MailchimpData data = content.getOriginalEvent();
        MatcherAssert.assertThat(data, notNullValue());
    }

    @Test
    void testExtractNewsDto() {
        // given
        NewsKoderiaEventDto baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/news_body.json", NewsKoderiaEventDto.class);
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEvent));
        
        // when
        MailChimpMessage mappedNotificationIntent = eventConverter.apply(genericEvent);

        // then
        NewsEventDataDto newsEventData = baseKoderiaEvent.getData();
        MailchimpContent content = mappedNotificationIntent.getBody();
        MatcherAssert.assertThat(mappedNotificationIntent.getHeader().getFlowId(), equalTo("default-flow"));
        MatcherAssert.assertThat(content.getSubject(), equalTo(newsEventData.getSubject()));
        // and
        MailchimpData data = content.getOriginalEvent();
        MatcherAssert.assertThat(data, notNullValue());
    }

}