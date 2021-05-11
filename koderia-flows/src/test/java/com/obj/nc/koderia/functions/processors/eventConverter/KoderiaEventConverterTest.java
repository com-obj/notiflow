package com.obj.nc.koderia.functions.processors.eventConverter;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.content.mailchimp.MailchimpData;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfigProperties;
import com.obj.nc.koderia.domain.event.*;
import com.obj.nc.koderia.domain.eventData.*;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfig;
import com.obj.nc.mappers.MailchimpDataToMailchimpContentMapper;
import com.obj.nc.utils.JsonUtils;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@JsonTest(properties = {
        "nc.flows.input-evet-routing.type-propery-name=type"
})
@AutoConfigureWebClient
@ContextConfiguration(classes = {
        KoderiaEventConverter.class,
        MailchimpDataToMailchimpContentMapper.class,
        KoderiaEventConverterConfig.class,
        MailchimpSenderConfigProperties.class
})
class KoderiaEventConverterTest {

    @Autowired private KoderiaEventConverter eventConverter;

    @Test
    void testExtractJobPostDto() {
        // given
        JobPostKoderiaEventDto baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", JobPostKoderiaEventDto.class);
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEvent));
    
        // when
        NotificationIntent mappedNotificationIntent = eventConverter.apply(genericEvent);

        // then
        MatcherAssert.assertThat(mappedNotificationIntent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/job_post_event_schema.json"));

        // and
        JobPostEventDataDto jobPostEventData = baseKoderiaEvent.getData();
        MailchimpContent content = mappedNotificationIntent.getBody().getContentTyped();
        MatcherAssert.assertThat(mappedNotificationIntent.getHeader().getFlowId(), equalTo("default-flow"));
        MatcherAssert.assertThat(content.getMessage().getSubject(), equalTo(jobPostEventData.getName()));
        // and
        MailchimpData data = content.getMessage().getMailchimpData();
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
    void testExtractNullSubjectEmitEventDto() {
        JobPostKoderiaEventDto baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", JobPostKoderiaEventDto.class);
        JobPostEventDataDto jobPostEventData = baseKoderiaEvent.getData();
        jobPostEventData.setName(null);
    
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEvent));

        // when - then
        Assertions.assertThatThrownBy(() -> eventConverter.apply(genericEvent))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Subject of Koderia event must not be null");
    }

    @Test
    void testExtractNullTextEmitEventDto() {
        JobPostKoderiaEventDto baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", JobPostKoderiaEventDto.class);
        JobPostEventDataDto jobPostEventData = baseKoderiaEvent.getData();
        jobPostEventData.setDescription(null);
    
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEvent));

        // when - then
        Assertions.assertThatThrownBy(() -> eventConverter.apply(genericEvent))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Text of Koderia event must not be null");
    }

    @Test
    void testExtractBlogDto() {
        // given
        BlogKoderiaEventDto baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/blog_body.json", BlogKoderiaEventDto.class);
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEvent));

        // when
        NotificationIntent mappedNotificationIntent = eventConverter.apply(genericEvent);

        // then
        MatcherAssert.assertThat(mappedNotificationIntent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/blog_event_schema.json"));

        // and
        BlogEventDataDto blogEventData = baseKoderiaEvent.getData();
        MailchimpContent content = mappedNotificationIntent.getBody().getContentTyped();
        MatcherAssert.assertThat(mappedNotificationIntent.getHeader().getFlowId(), equalTo("default-flow"));
        MatcherAssert.assertThat(content.getMessage().getSubject(), equalTo(blogEventData.getTitle()));
        // and
        MailchimpData data = content.getMessage().getMailchimpData();
        MatcherAssert.assertThat(data, notNullValue());
    }

    @Test
    void testExtractEventDto() {
        // given
        EventKoderiaEventDto baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/event_body.json", EventKoderiaEventDto.class);
    
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEvent));
        
        // when
        NotificationIntent mappedNotificationIntent = eventConverter.apply(genericEvent);

        // then
        MatcherAssert.assertThat(mappedNotificationIntent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/event_event_schema.json"));

        // and
        EventEventDataDto eventEventData = baseKoderiaEvent.getData();
        MailchimpContent content = mappedNotificationIntent.getBody().getContentTyped();
        MatcherAssert.assertThat(mappedNotificationIntent.getHeader().getFlowId(), equalTo("default-flow"));
        MatcherAssert.assertThat(content.getMessage().getSubject(), equalTo(eventEventData.getName()));
        // and
        MailchimpData data = content.getMessage().getMailchimpData();
        MatcherAssert.assertThat(data, notNullValue());
    }

    @Test
    void testExtractLinkDto() {
        // given
        LinkKoderiaEventDto baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/link_body.json", LinkKoderiaEventDto.class);
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEvent));

        // when
        NotificationIntent mappedNotificationIntent = eventConverter.apply(genericEvent);

        // then
        MatcherAssert.assertThat(mappedNotificationIntent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/link_event_schema.json"));

        // and
        LinkEventDataDto linkEventData = baseKoderiaEvent.getData();
        MailchimpContent content = mappedNotificationIntent.getBody().getContentTyped();
        MatcherAssert.assertThat(mappedNotificationIntent.getHeader().getFlowId(), equalTo("default-flow"));
        MatcherAssert.assertThat(content.getMessage().getSubject(), equalTo(linkEventData.getTitle()));
        // and
        MailchimpData data = content.getMessage().getMailchimpData();
        MatcherAssert.assertThat(data, notNullValue());
    }

    @Test
    void testExtractNewsDto() {
        // given
        NewsKoderiaEventDto baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/news_body.json", NewsKoderiaEventDto.class);
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEvent));
        
        // when
        NotificationIntent mappedNotificationIntent = eventConverter.apply(genericEvent);

        // then
        MatcherAssert.assertThat(mappedNotificationIntent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/news_event_schema.json"));

        // and
        NewsEventDataDto newsEventData = baseKoderiaEvent.getData();
        MailchimpContent content = mappedNotificationIntent.getBody().getContentTyped();
        MatcherAssert.assertThat(mappedNotificationIntent.getHeader().getFlowId(), equalTo("default-flow"));
        MatcherAssert.assertThat(content.getMessage().getSubject(), equalTo(newsEventData.getSubject()));
        // and
        MailchimpData data = content.getMessage().getMailchimpData();
        MatcherAssert.assertThat(data, notNullValue());
    }

}