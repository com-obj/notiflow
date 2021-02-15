package com.obj.nc.functions.processors;

import com.obj.nc.domain.event.Event;
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
        EmitJobPostEventDto emitJobPostEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", EmitJobPostEventDto.class);

        // when
        Event mappedEvent = koderiaEventConverter.apply(emitJobPostEventDto);

        // then
        MatcherAssert.assertThat(mappedEvent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/job_post_event_schema.json"));

        // and
        MatcherAssert.assertThat(mappedEvent.getHeader().getConfigurationName(), Matchers.equalTo("static-routing-pipeline"));
        MatcherAssert.assertThat(mappedEvent.getBody().getMessage().getContent().getSubject(), Matchers.equalTo(emitJobPostEventDto.getSubject()));
        MatcherAssert.assertThat(mappedEvent.getBody().getMessage().getContent().getText(), Matchers.equalTo(emitJobPostEventDto.getText()));

        // and
        Map<String, Object> eventAttributes = mappedEvent.getBody().getAttributes();
        MatcherAssert.assertThat(eventAttributes.get("id"), Matchers.equalTo(emitJobPostEventDto.getId()));
        MatcherAssert.assertThat(eventAttributes.get("location"), Matchers.equalTo(emitJobPostEventDto.getLocation()));
        MatcherAssert.assertThat(eventAttributes.get("rate"), Matchers.equalTo(emitJobPostEventDto.getRate()));
        MatcherAssert.assertThat(eventAttributes.get("technologies"), Matchers.equalTo(emitJobPostEventDto.getTechnologies()));
        MatcherAssert.assertThat(eventAttributes.get("specialRate"), Matchers.equalTo(emitJobPostEventDto.getSpecialRate()));
        MatcherAssert.assertThat(eventAttributes.get("labels"), Matchers.equalTo(emitJobPostEventDto.getLabels()));
        MatcherAssert.assertThat(eventAttributes.get("positionType"), Matchers.equalTo(emitJobPostEventDto.getPositionType()));
        MatcherAssert.assertThat(eventAttributes.get("duration"), Matchers.equalTo(emitJobPostEventDto.getDuration()));
        MatcherAssert.assertThat(eventAttributes.get("jobType"), Matchers.equalTo(emitJobPostEventDto.getJobType()));
        MatcherAssert.assertThat(eventAttributes.get("dateOfStart"), Matchers.equalTo(emitJobPostEventDto.getDateOfStart()));
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
        EmitEventDto emitEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", EmitJobPostEventDto.class);
        emitEventDto.setSubject(null);

        // when - then
        Assertions.assertThatThrownBy(() -> koderiaEventConverter.apply(emitEventDto))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Subject of Koderia event must not be null");
    }

    @Test
    void testConvertNullTextEmitEventDto() {
        EmitEventDto emitEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", EmitJobPostEventDto.class);
        emitEventDto.setText(null);

        // when - then
        Assertions.assertThatThrownBy(() -> koderiaEventConverter.apply(emitEventDto))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Text of Koderia event must not be null");
    }

    @Test
    void testConvertBlogDto() {
        // given
        EmitBlogEventDto emitJobPostEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/blog_body.json", EmitBlogEventDto.class);

        // when
        Event mappedEvent = koderiaEventConverter.apply(emitJobPostEventDto);

        // then
        MatcherAssert.assertThat(mappedEvent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/blog_event_schema.json"));

        // and
        MatcherAssert.assertThat(mappedEvent.getHeader().getConfigurationName(), Matchers.equalTo("static-routing-pipeline"));
        MatcherAssert.assertThat(mappedEvent.getBody().getMessage().getContent().getSubject(), Matchers.equalTo(emitJobPostEventDto.getSubject()));
        MatcherAssert.assertThat(mappedEvent.getBody().getMessage().getContent().getText(), Matchers.equalTo(emitJobPostEventDto.getText()));

        // and
        Map<String, Object> eventAttributes = mappedEvent.getBody().getAttributes();
        MatcherAssert.assertThat(eventAttributes.get("id"), Matchers.equalTo(emitJobPostEventDto.getId()));
        MatcherAssert.assertThat(eventAttributes.get("link"), Matchers.equalTo(emitJobPostEventDto.getLink()));
        MatcherAssert.assertThat(eventAttributes.get("featuredImage"), Matchers.equalTo(emitJobPostEventDto.getFeaturedImage()));

        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("category")).get("id"), Matchers.equalTo(emitJobPostEventDto.getCategory().getId()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("category")).get("slug"), Matchers.equalTo(emitJobPostEventDto.getCategory().getSlug()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("category")).get("name"), Matchers.equalTo(emitJobPostEventDto.getCategory().getName()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("category")).get("color_b"), Matchers.equalTo(emitJobPostEventDto.getCategory().getColorB()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("category")).get("color_a"), Matchers.equalTo(emitJobPostEventDto.getCategory().getColorA()));

        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("author")).get("avatar"), Matchers.equalTo(emitJobPostEventDto.getAuthor().getAvatar()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("author")).get("description"), Matchers.equalTo(emitJobPostEventDto.getAuthor().getDescription()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("author")).get("name"), Matchers.equalTo(emitJobPostEventDto.getAuthor().getName()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("author")).get("id"), Matchers.equalTo(emitJobPostEventDto.getAuthor().getId()));
    }

    @Test
    void testConvertEventDto() {
        // given
        EmitEventEventDto emitJobPostEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/event_body.json", EmitEventEventDto.class);

        // when
        Event mappedEvent = koderiaEventConverter.apply(emitJobPostEventDto);

        // then
        MatcherAssert.assertThat(mappedEvent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/event_event_schema.json"));

        // and
        MatcherAssert.assertThat(mappedEvent.getHeader().getConfigurationName(), Matchers.equalTo("static-routing-pipeline"));
        MatcherAssert.assertThat(mappedEvent.getBody().getMessage().getContent().getSubject(), Matchers.equalTo(emitJobPostEventDto.getSubject()));
        MatcherAssert.assertThat(mappedEvent.getBody().getMessage().getContent().getText(), Matchers.equalTo(emitJobPostEventDto.getText()));

        // and
        Map<String, Object> eventAttributes = mappedEvent.getBody().getAttributes();
        MatcherAssert.assertThat(eventAttributes.get("id"), Matchers.equalTo(emitJobPostEventDto.getId()));
        MatcherAssert.assertThat(eventAttributes.get("ticketUrl"), Matchers.equalTo(emitJobPostEventDto.getTicketUrl()));
        MatcherAssert.assertThat(eventAttributes.get("startAt"), Matchers.equalTo(emitJobPostEventDto.getStartAt()));
        MatcherAssert.assertThat(eventAttributes.get("locationId"), Matchers.equalTo(emitJobPostEventDto.getLocationId()));
        MatcherAssert.assertThat(eventAttributes.get("endAt"), Matchers.equalTo(emitJobPostEventDto.getEndAt()));

        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("location")).get("googlePlaceId"), Matchers.equalTo(emitJobPostEventDto.getLocation().getGooglePlaceId()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("location")).get("name"), Matchers.equalTo(emitJobPostEventDto.getLocation().getName()));

        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("organizer")).get("description"), Matchers.equalTo(emitJobPostEventDto.getOrganizer().getDescription()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("organizer")).get("url"), Matchers.equalTo(emitJobPostEventDto.getOrganizer().getUrl()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("organizer")).get("imageUrl"), Matchers.equalTo(emitJobPostEventDto.getOrganizer().getImageUrl()));
        MatcherAssert.assertThat(((Map<String, Object>) eventAttributes.get("organizer")).get("name"), Matchers.equalTo(emitJobPostEventDto.getOrganizer().getName()));

        MatcherAssert.assertThat(eventAttributes.get("imageUrl"), Matchers.equalTo(emitJobPostEventDto.getImageUrl()));
    }

    @Test
    void testConvertLinkDto() {
        // given
        EmitLinkEventDto emitJobPostEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/link_body.json", EmitLinkEventDto.class);

        // when
        Event mappedEvent = koderiaEventConverter.apply(emitJobPostEventDto);

        // then
        MatcherAssert.assertThat(mappedEvent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/link_event_schema.json"));

        // and
        MatcherAssert.assertThat(mappedEvent.getHeader().getConfigurationName(), Matchers.equalTo("static-routing-pipeline"));
        MatcherAssert.assertThat(mappedEvent.getBody().getMessage().getContent().getSubject(), Matchers.equalTo(emitJobPostEventDto.getSubject()));
        MatcherAssert.assertThat(mappedEvent.getBody().getMessage().getContent().getText(), Matchers.equalTo(emitJobPostEventDto.getText()));

        // and
        Map<String, Object> eventAttributes = mappedEvent.getBody().getAttributes();
        MatcherAssert.assertThat(eventAttributes.get("id"), Matchers.equalTo(emitJobPostEventDto.getId()));
        MatcherAssert.assertThat(eventAttributes.get("siteName"), Matchers.equalTo(emitJobPostEventDto.getSiteName()));
        MatcherAssert.assertThat(eventAttributes.get("images"), Matchers.equalTo(emitJobPostEventDto.getImages()));
        MatcherAssert.assertThat(eventAttributes.get("url"), Matchers.equalTo(emitJobPostEventDto.getUrl()));
        MatcherAssert.assertThat(eventAttributes.get("favicons"), Matchers.equalTo(emitJobPostEventDto.getFavicons()));

    }

    @Test
    void testConvertNewsDto() {
        // given
        EmitNewsEventDto emitJobPostEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/news_body.json", EmitNewsEventDto.class);

        // when
        Event mappedEvent = koderiaEventConverter.apply(emitJobPostEventDto);

        // then
        MatcherAssert.assertThat(mappedEvent.toJSONString(), JsonSchemaValidator.matchesJsonSchemaInClasspath("koderia/event_schema/news_event_schema.json"));

        // and
        MatcherAssert.assertThat(mappedEvent.getHeader().getConfigurationName(), Matchers.equalTo("static-routing-pipeline"));
        MatcherAssert.assertThat(mappedEvent.getBody().getMessage().getContent().getSubject(), Matchers.equalTo(emitJobPostEventDto.getSubject()));
        MatcherAssert.assertThat(mappedEvent.getBody().getMessage().getContent().getText(), Matchers.equalTo(emitJobPostEventDto.getText()));
    }

}