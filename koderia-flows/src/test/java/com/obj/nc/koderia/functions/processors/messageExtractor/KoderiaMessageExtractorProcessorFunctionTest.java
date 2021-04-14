package com.obj.nc.koderia.functions.processors.messageExtractor;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.eventFactory.GenericEventToNotificaitonIntentConverter;
import com.obj.nc.koderia.KoderiaFlowsApplication;
import com.obj.nc.koderia.dto.koderia.data.*;
import com.obj.nc.koderia.dto.koderia.event.BaseKoderiaEventDto;
import com.obj.nc.utils.JsonUtils;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.obj.nc.functions.processors.eventFactory.GenericEventToNotificaitonIntentConverter.ORIGINAL_EVENT_FIELD;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest(classes = {
        KoderiaFlowsApplication.class, 
        GenericEventToNotificaitonIntentConverter.class
})
class KoderiaMessageExtractorProcessorFunctionTest {

    @Autowired private KoderiaMessageExtractorProcessorFunction messageExtractor;
    @Autowired private GenericEventToNotificaitonIntentConverter eventConverter;

    @Test
    void testExtractJobPostDto() {
        // given
        BaseKoderiaEventDto baseKoderiaEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", BaseKoderiaEventDto.class);
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEventDto));
        NotificationIntent notifIntent = eventConverter.apply(genericEvent);
        
        // when
        NotificationIntent mappedNotificationIntent = messageExtractor.apply(notifIntent);
    
        // then
        MatcherAssert.assertThat(mappedNotificationIntent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/job_post_event_schema.json"));
    
        // and
        JobPostEventDataDto jobPostEventData = baseKoderiaEventDto.getData();
        EmailContent emailContent = mappedNotificationIntent.getContentTyped();
        
        MatcherAssert.assertThat(mappedNotificationIntent.getHeader().getFlowId(), Matchers.equalTo("default-flow"));
        MatcherAssert.assertThat(emailContent.getSubject(), Matchers.equalTo(jobPostEventData.getName()));
        MatcherAssert.assertThat(emailContent.getText(), Matchers.equalTo(jobPostEventData.getDescription()));
        MatcherAssert.assertThat(mappedNotificationIntent.getAttributeValue(ORIGINAL_EVENT_FIELD), Matchers.nullValue());
        MatcherAssert.assertThat(emailContent.getAttributeValue(ORIGINAL_EVENT_FIELD), Matchers.notNullValue());
    }

    @Test
    void testExtractNullEmitEventDto() {
        // when - then
        Assertions.assertThatThrownBy(() -> messageExtractor.apply(null))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Koderia event must not be null");
    }

    @Test
    void testExtractNullSubjectEmitEventDto() {
        BaseKoderiaEventDto baseKoderiaEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", BaseKoderiaEventDto.class);
        JobPostEventDataDto jobPostEventData = baseKoderiaEventDto.getData();
        jobPostEventData.setName(null);
    
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEventDto));
        NotificationIntent notifIntent = eventConverter.apply(genericEvent);

        // when - then
        Assertions.assertThatThrownBy(() -> messageExtractor.apply(notifIntent))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Subject of Koderia event must not be null");
    }

    @Test
    void testExtractNullTextEmitEventDto() {
        BaseKoderiaEventDto baseKoderiaEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", BaseKoderiaEventDto.class);
        JobPostEventDataDto jobPostEventData = baseKoderiaEventDto.getData();
        jobPostEventData.setDescription(null);
    
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEventDto));
        NotificationIntent notifIntent = eventConverter.apply(genericEvent);

        // when - then
        Assertions.assertThatThrownBy(() -> messageExtractor.apply(notifIntent))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Text of Koderia event must not be null");
    }

    @Test
    void testExtractBlogDto() {
        // given
        BaseKoderiaEventDto baseKoderiaEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/blog_body.json", BaseKoderiaEventDto.class);
        
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEventDto));
        NotificationIntent notifIntent = eventConverter.apply(genericEvent);

        // when
        NotificationIntent mappedNotificationIntent = messageExtractor.apply(notifIntent);
        System.out.println(mappedNotificationIntent.toJSONString());

        // then
        MatcherAssert.assertThat(mappedNotificationIntent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/blog_event_schema.json"));

        // and
        BlogEventDataDto blogEventData = baseKoderiaEventDto.getData();
        EmailContent emailContent = mappedNotificationIntent.getBody().getContentTyped();
        MatcherAssert.assertThat(mappedNotificationIntent.getHeader().getFlowId(), Matchers.equalTo("default-flow"));
        MatcherAssert.assertThat(emailContent.getSubject(), Matchers.equalTo(blogEventData.getTitle()));
        MatcherAssert.assertThat(emailContent.getText(), Matchers.equalTo(blogEventData.getContent()));
        MatcherAssert.assertThat(mappedNotificationIntent.getAttributeValue(ORIGINAL_EVENT_FIELD), Matchers.nullValue());
        MatcherAssert.assertThat(emailContent.getAttributeValue(ORIGINAL_EVENT_FIELD), Matchers.notNullValue());
    }

    @Test
    void testExtractEventDto() {
        // given
        BaseKoderiaEventDto baseKoderiaEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/event_body.json", BaseKoderiaEventDto.class);
    
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEventDto));
        NotificationIntent notifIntent = eventConverter.apply(genericEvent);
        
        // when
        NotificationIntent mappedNotificationIntent = messageExtractor.apply(notifIntent);
        System.out.println(mappedNotificationIntent.toJSONString());

        // then
        MatcherAssert.assertThat(mappedNotificationIntent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/event_event_schema.json"));

        // and
        EventEventDataDto eventEventData = baseKoderiaEventDto.getData();
        EmailContent emailContent = mappedNotificationIntent.getBody().getContentTyped();
        MatcherAssert.assertThat(mappedNotificationIntent.getHeader().getFlowId(), Matchers.equalTo("default-flow"));
        MatcherAssert.assertThat(emailContent.getSubject(), Matchers.equalTo(eventEventData.getName()));
        MatcherAssert.assertThat(emailContent.getText(), Matchers.equalTo(eventEventData.getDescription()));
        MatcherAssert.assertThat(mappedNotificationIntent.getAttributeValue(ORIGINAL_EVENT_FIELD), Matchers.nullValue());
        MatcherAssert.assertThat(emailContent.getAttributeValue(ORIGINAL_EVENT_FIELD), Matchers.notNullValue());
    }

    @Test
    void testExtractLinkDto() {
        // given
        BaseKoderiaEventDto baseKoderiaEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/link_body.json", BaseKoderiaEventDto.class);
    
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEventDto));
        NotificationIntent notifIntent = eventConverter.apply(genericEvent);

        // when
        NotificationIntent mappedNotificationIntent = messageExtractor.apply(notifIntent);
        System.out.println(mappedNotificationIntent.toJSONString());

        // then
        MatcherAssert.assertThat(mappedNotificationIntent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/link_event_schema.json"));

        // and
        LinkEventDataDto linkEventData = baseKoderiaEventDto.getData();
        EmailContent emailContent = mappedNotificationIntent.getBody().getContentTyped();
        MatcherAssert.assertThat(mappedNotificationIntent.getHeader().getFlowId(), Matchers.equalTo("default-flow"));
        MatcherAssert.assertThat(emailContent.getSubject(), Matchers.equalTo(linkEventData.getTitle()));
        MatcherAssert.assertThat(emailContent.getText(), Matchers.equalTo(linkEventData.getDescription()));
        MatcherAssert.assertThat(mappedNotificationIntent.getAttributeValue(ORIGINAL_EVENT_FIELD), Matchers.nullValue());
        MatcherAssert.assertThat(emailContent.getAttributeValue(ORIGINAL_EVENT_FIELD), Matchers.notNullValue());
    }

    @Test
    void testExtractNewsDto() {
        // given
        BaseKoderiaEventDto baseKoderiaEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/news_body.json", BaseKoderiaEventDto.class);
    
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEventDto));
        NotificationIntent notifIntent = eventConverter.apply(genericEvent);
        
        // when
        NotificationIntent mappedNotificationIntent = messageExtractor.apply(notifIntent);
        System.out.println(mappedNotificationIntent.toJSONString());

        // then
        MatcherAssert.assertThat(mappedNotificationIntent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/news_event_schema.json"));

        // and
        NewsEventDataDto newsEventData = baseKoderiaEventDto.getData();
        EmailContent emailContent = mappedNotificationIntent.getBody().getContentTyped();
        MatcherAssert.assertThat(mappedNotificationIntent.getHeader().getFlowId(), Matchers.equalTo("default-flow"));
        MatcherAssert.assertThat(emailContent.getSubject(), Matchers.equalTo(newsEventData.getSubject()));
        MatcherAssert.assertThat(emailContent.getText(), Matchers.equalTo(newsEventData.getText()));
        MatcherAssert.assertThat(mappedNotificationIntent.getAttributeValue(ORIGINAL_EVENT_FIELD), Matchers.nullValue());
        MatcherAssert.assertThat(emailContent.getAttributeValue(ORIGINAL_EVENT_FIELD), Matchers.notNullValue());
    }

}