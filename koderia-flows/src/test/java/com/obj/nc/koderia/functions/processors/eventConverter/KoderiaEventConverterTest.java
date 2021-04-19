package com.obj.nc.koderia.functions.processors.eventConverter;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.koderia.domain.eventData.*;
import com.obj.nc.koderia.domain.event.BaseKoderiaEvent;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfig;
import com.obj.nc.koderia.mapper.KoderiaEvent2MailchimpContentMapper;
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

import static com.obj.nc.domain.content.mailchimp.MailchimpContent.DATA_MERGE_VARIABLE;
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@JsonTest(properties = {
        "nc.flows.input-evet-routing.type-propery-name=type"
})
@AutoConfigureWebClient
@ContextConfiguration(classes = {
        KoderiaEventConverter.class,
        KoderiaEvent2MailchimpContentMapper.class,
        KoderiaEventConverterConfig.class,
        MailchimpSenderConfig.class
})
class KoderiaEventConverterTest {

    @Autowired private KoderiaEventConverter eventConverter;

    @Test
    void testExtractJobPostDto() {
        // given
        BaseKoderiaEvent baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", BaseKoderiaEvent.class);
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
        Optional<Object> dataVariable = content.getMessage().findMergeVariableContentByName(DATA_MERGE_VARIABLE);
        MatcherAssert.assertThat(dataVariable.isPresent(), equalTo(true));
        MatcherAssert.assertThat(((BaseKoderiaEvent) dataVariable.get()).getMessageText(), equalTo(jobPostEventData.getDescription()));
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
        BaseKoderiaEvent baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", BaseKoderiaEvent.class);
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
        BaseKoderiaEvent baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", BaseKoderiaEvent.class);
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
        BaseKoderiaEvent baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/blog_body.json", BaseKoderiaEvent.class);
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
        Optional<Object> dataVariable = content.getMessage().findMergeVariableContentByName(DATA_MERGE_VARIABLE);
        MatcherAssert.assertThat(dataVariable.isPresent(), equalTo(true));
        MatcherAssert.assertThat(((BaseKoderiaEvent) dataVariable.get()).getMessageText(), equalTo(blogEventData.getContent()));
    }

    @Test
    void testExtractEventDto() {
        // given
        BaseKoderiaEvent baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/event_body.json", BaseKoderiaEvent.class);
    
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
        Optional<Object> dataVariable = content.getMessage().findMergeVariableContentByName(DATA_MERGE_VARIABLE);
        MatcherAssert.assertThat(dataVariable.isPresent(), equalTo(true));
        MatcherAssert.assertThat(((BaseKoderiaEvent) dataVariable.get()).getMessageText(), equalTo(eventEventData.getDescription()));
    }

    @Test
    void testExtractLinkDto() {
        // given
        BaseKoderiaEvent baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/link_body.json", BaseKoderiaEvent.class);
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
        Optional<Object> dataVariable = content.getMessage().findMergeVariableContentByName(DATA_MERGE_VARIABLE);
        MatcherAssert.assertThat(dataVariable.isPresent(), equalTo(true));
        MatcherAssert.assertThat(((BaseKoderiaEvent) dataVariable.get()).getMessageText(), equalTo(linkEventData.getDescription()));
    }

    @Test
    void testExtractNewsDto() {
        // given
        BaseKoderiaEvent baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/news_body.json", BaseKoderiaEvent.class);
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
        Optional<Object> dataVariable = content.getMessage().findMergeVariableContentByName(DATA_MERGE_VARIABLE);
        MatcherAssert.assertThat(dataVariable.isPresent(), equalTo(true));
        MatcherAssert.assertThat(((BaseKoderiaEvent) dataVariable.get()).getMessageText(), equalTo(newsEventData.getText()));
    }

}