package com.obj.nc.functions.processors;

import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.message.Email;
import com.obj.nc.dto.*;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.utils.JsonUtils;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Map;

import static com.obj.nc.functions.processors.KoderiaEventConverterExecution.ORIGINAL_EVENT_FIELD;

@SpringJUnitConfig(classes = {
        KoderiaEventConverterProcessingFunction.class,
        KoderiaEventConverterExecution.class,
        KoderiaEventConverterPreCondition.class
})
class KoderiaEventConverterProcessingFunctionTest {

    @Autowired
    private KoderiaEventConverterProcessingFunction koderiaEventConverter;

    @Test
    void testConvertJobPostDto() {
        // given
        EmitEventDto emitEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", EmitEventDto.class);
        JobPostEventDataDto jobPostEventData = (JobPostEventDataDto) emitEventDto.getData();

        // when
        Event mappedEvent = koderiaEventConverter.apply(emitEventDto);

        // then
        MatcherAssert.assertThat(mappedEvent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/job_post_event_schema.json"));

        // and
        Email email = mappedEvent.getBody().getContentTyped();
        MatcherAssert.assertThat(mappedEvent.getHeader().getFlowId(), Matchers.equalTo("static-routing-pipeline"));
        MatcherAssert.assertThat(email.getSubject(), Matchers.equalTo(jobPostEventData.getMessageSubject()));
        MatcherAssert.assertThat(email.getText(), Matchers.equalTo(jobPostEventData.getMessageText()));

        // and
        Map<String, Object> eventAttributes = (Map<String, Object>) ((Map<String, Object>) mappedEvent.getBody().getMessage().getAttributes().get(ORIGINAL_EVENT_FIELD)).get("data");
        MatcherAssert.assertThat(eventAttributes.get("id"), Matchers.equalTo(jobPostEventData.getId()));
        MatcherAssert.assertThat(eventAttributes.get("location"), Matchers.equalTo(jobPostEventData.getLocation()));
        MatcherAssert.assertThat(eventAttributes.get("rate"), Matchers.equalTo(jobPostEventData.getRate()));
        MatcherAssert.assertThat(eventAttributes.get("technologies"), Matchers.equalTo(jobPostEventData.getTechnologies()));
        MatcherAssert.assertThat(eventAttributes.get("specialRate"), Matchers.equalTo(jobPostEventData.getSpecialRate()));
        MatcherAssert.assertThat(eventAttributes.get("labels"), Matchers.equalTo(jobPostEventData.getLabels()));
        MatcherAssert.assertThat(eventAttributes.get("positionType"), Matchers.equalTo(jobPostEventData.getPositionType()));
        MatcherAssert.assertThat(eventAttributes.get("duration"), Matchers.equalTo(jobPostEventData.getDuration()));
        MatcherAssert.assertThat(eventAttributes.get("type"), Matchers.equalTo(jobPostEventData.getType()));
        MatcherAssert.assertThat(eventAttributes.get("dateOfStart"), Matchers.equalTo(jobPostEventData.getDateOfStart()));
    }

    @Test
    void testConvertNullEmitEventDto() {
        // when - then
        Assertions.assertThatThrownBy(() -> koderiaEventConverter.apply(null))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Koderia event must not be null");
    }

    @Test
    void testConvertNullSubjectEmitEventDto() {
        EmitEventDto emitEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", EmitEventDto.class);
        JobPostEventDataDto jobPostEventData = (JobPostEventDataDto) emitEventDto.getData();
        jobPostEventData.setName(null);

        // when - then
        Assertions.assertThatThrownBy(() -> koderiaEventConverter.apply(emitEventDto))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Subject of Koderia event must not be null");
    }

    @Test
    void testConvertNullTextEmitEventDto() {
        EmitEventDto emitEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", EmitEventDto.class);
        JobPostEventDataDto jobPostEventData = (JobPostEventDataDto) emitEventDto.getData();
        jobPostEventData.setDescription(null);

        // when - then
        Assertions.assertThatThrownBy(() -> koderiaEventConverter.apply(emitEventDto))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Text of Koderia event must not be null");
    }

    @Test
    void testConvertBlogDto() {
        // given
        EmitEventDto emitEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/blog_body.json", EmitEventDto.class);
        BlogEventDataDto blogEventData = (BlogEventDataDto) emitEventDto.getData();

        // when
        Event mappedEvent = koderiaEventConverter.apply(emitEventDto);

        // then
        MatcherAssert.assertThat(mappedEvent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/blog_event_schema.json"));

        // and
        Email email = mappedEvent.getBody().getContentTyped();
        MatcherAssert.assertThat(mappedEvent.getHeader().getFlowId(), Matchers.equalTo("static-routing-pipeline"));
        MatcherAssert.assertThat(email.getSubject(), Matchers.equalTo(blogEventData.getTitle()));
        MatcherAssert.assertThat(email.getText(), Matchers.equalTo(blogEventData.getContent()));

        // and
        Map<String, Object> eventAttributes = (Map<String, Object>) ((Map<String, Object>) mappedEvent.getBody().getMessage().getAttributes().get(ORIGINAL_EVENT_FIELD)).get("data");
        MatcherAssert.assertThat(eventAttributes.get("id"), Matchers.equalTo(blogEventData.getId()));
        MatcherAssert.assertThat(eventAttributes.get("link"), Matchers.equalTo(blogEventData.getLink()));
        MatcherAssert.assertThat(eventAttributes.get("featuredImage"), Matchers.equalTo(blogEventData.getFeaturedImage()));

        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("category")).get("id"), Matchers.equalTo(blogEventData.getCategory().getId()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("category")).get("slug"), Matchers.equalTo(blogEventData.getCategory().getSlug()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("category")).get("name"), Matchers.equalTo(blogEventData.getCategory().getName()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("category")).get("color_b"), Matchers.equalTo(blogEventData.getCategory().getColorB()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("category")).get("color_a"), Matchers.equalTo(blogEventData.getCategory().getColorA()));

        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("author")).get("avatar"), Matchers.equalTo(blogEventData.getAuthor().getAvatar()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("author")).get("description"), Matchers.equalTo(blogEventData.getAuthor().getDescription()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("author")).get("name"), Matchers.equalTo(blogEventData.getAuthor().getName()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("author")).get("id"), Matchers.equalTo(blogEventData.getAuthor().getId()));
    }

    @Test
    void testConvertEventDto() {
        // given
        EmitEventDto emitEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/event_body.json", EmitEventDto.class);
        EventEventDataDto eventEventData = (EventEventDataDto) emitEventDto.getData();

        // when
        Event mappedEvent = koderiaEventConverter.apply(emitEventDto);

        // then
        MatcherAssert.assertThat(mappedEvent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/event_event_schema.json"));

        // and
        Email email = mappedEvent.getBody().getContentTyped();
        MatcherAssert.assertThat(mappedEvent.getHeader().getFlowId(), Matchers.equalTo("static-routing-pipeline"));
        MatcherAssert.assertThat(email.getSubject(), Matchers.equalTo(eventEventData.getName()));
        MatcherAssert.assertThat(email.getText(), Matchers.equalTo(eventEventData.getDescription()));

        // and
        Map<String, Object> eventAttributes = (Map<String, Object>) ((Map<String, Object>) mappedEvent.getBody().getMessage().getAttributes().get(ORIGINAL_EVENT_FIELD)).get("data");
        MatcherAssert.assertThat(eventAttributes.get("id"), Matchers.equalTo(eventEventData.getId()));
        MatcherAssert.assertThat(eventAttributes.get("ticketUrl"), Matchers.equalTo(eventEventData.getTicketUrl()));
        MatcherAssert.assertThat(eventAttributes.get("startAt"), Matchers.equalTo(eventEventData.getStartAt()));
        MatcherAssert.assertThat(eventAttributes.get("locationId"), Matchers.equalTo(eventEventData.getLocationId()));
        MatcherAssert.assertThat(eventAttributes.get("endAt"), Matchers.equalTo(eventEventData.getEndAt()));

        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("location")).get("googlePlaceId"), Matchers.equalTo(eventEventData.getLocation().getGooglePlaceId()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("location")).get("name"), Matchers.equalTo(eventEventData.getLocation().getName()));

        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("organizer")).get("description"), Matchers.equalTo(eventEventData.getOrganizer().getDescription()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("organizer")).get("url"), Matchers.equalTo(eventEventData.getOrganizer().getUrl()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("organizer")).get("imageUrl"), Matchers.equalTo(eventEventData.getOrganizer().getImageUrl()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("organizer")).get("name"), Matchers.equalTo(eventEventData.getOrganizer().getName()));

        MatcherAssert.assertThat(eventAttributes.get("imageUrl"), Matchers.equalTo(eventEventData.getImageUrl()));
    }

    @Test
    void testConvertLinkDto() {
        // given
        EmitEventDto emitEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/link_body.json", EmitEventDto.class);
        LinkEventDataDto linkEventData = (LinkEventDataDto) emitEventDto.getData();

        // when
        Event mappedEvent = koderiaEventConverter.apply(emitEventDto);

        // then
        MatcherAssert.assertThat(mappedEvent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/link_event_schema.json"));

        // and
        Email email = mappedEvent.getBody().getContentTyped();
        MatcherAssert.assertThat(mappedEvent.getHeader().getFlowId(), Matchers.equalTo("static-routing-pipeline"));
        MatcherAssert.assertThat(email.getSubject(), Matchers.equalTo(linkEventData.getTitle()));
        MatcherAssert.assertThat(email.getText(), Matchers.equalTo(linkEventData.getDescription()));

        // and
        Map<String, Object> eventAttributes = (Map<String, Object>) ((Map<String, Object>) mappedEvent.getBody().getMessage().getAttributes().get(ORIGINAL_EVENT_FIELD)).get("data");
        MatcherAssert.assertThat(eventAttributes.get("id"), Matchers.equalTo(linkEventData.getId()));
        MatcherAssert.assertThat(eventAttributes.get("siteName"), Matchers.equalTo(linkEventData.getSiteName()));
        MatcherAssert.assertThat(eventAttributes.get("images"), Matchers.equalTo(linkEventData.getImages()));
        MatcherAssert.assertThat(eventAttributes.get("url"), Matchers.equalTo(linkEventData.getUrl()));
        MatcherAssert.assertThat(eventAttributes.get("favicons"), Matchers.equalTo(linkEventData.getFavicons()));
    }

    @Test
    void testConvertNewsDto() {
        // given
        EmitEventDto emitEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/news_body.json", EmitEventDto.class);
        NewsEventDataDto newsEventData = (NewsEventDataDto) emitEventDto.getData();

        // when
        Event mappedEvent = koderiaEventConverter.apply(emitEventDto);

        // then
        MatcherAssert.assertThat(mappedEvent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/news_event_schema.json"));

        // and
        Email email = mappedEvent.getBody().getContentTyped();
        MatcherAssert.assertThat(mappedEvent.getHeader().getFlowId(), Matchers.equalTo("static-routing-pipeline"));
        MatcherAssert.assertThat(email.getSubject(), Matchers.equalTo(newsEventData.getSubject()));
        MatcherAssert.assertThat(email.getText(), Matchers.equalTo(newsEventData.getText()));
    }

}