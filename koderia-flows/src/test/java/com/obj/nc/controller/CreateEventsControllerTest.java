package com.obj.nc.controller;

import com.obj.nc.domain.event.Event;
import com.obj.nc.dto.*;
import com.obj.nc.utils.JsonUtils;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.WebApplicationContext;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

@ActiveProfiles(profiles = "junit-test")
@WebMvcTest(controllers = CreateEventsController.class)
@ContextConfiguration(classes = CreateEventsControllerTestConfig.class)
class CreateEventsControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void initialiseRestAssuredMockMvcWebApplicationContext() {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
    }

    @Test
    void testCreateJobPostEvent() {
        CreateJobPostDto createJobPostDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", CreateJobPostDto.class);

        Event responseEvent = RestAssuredMockMvc
                .given()
                .config(RestAssuredMockMvcConfig.config().encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8")))
                .contentType(ContentType.JSON)
                .body(createJobPostDto)
                .when()
                .post("/events/create/jobPost")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .status(HttpStatus.CREATED)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("koderia/event_schema/job_post_event_schema.json"))
                .extract().as(Event.class);

        MatcherAssert.assertThat(responseEvent.getBody().getMessage().getContent().getSubject(), Matchers.equalTo(createJobPostDto.getSubject()));
        MatcherAssert.assertThat(responseEvent.getBody().getMessage().getContent().getText(), Matchers.equalTo(createJobPostDto.getText()));
        MatcherAssert.assertThat(responseEvent.getBody().getAttributes(), Matchers.equalTo(createJobPostDto.asMap()));
    }

    @Test
    void testCreateBlogEvent() {
        CreateBlogDto createBlogDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/blog_body.json", CreateBlogDto.class);

        Event responseEvent = RestAssuredMockMvc
                .given()
                .config(RestAssuredMockMvcConfig.config().encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8")))
                .contentType(ContentType.JSON)
                .body(createBlogDto)
                .when()
                .post("/events/create/blog")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .status(HttpStatus.CREATED)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("koderia/event_schema/blog_event_schema.json"))
                .extract().as(Event.class);

        MatcherAssert.assertThat(responseEvent.getBody().getMessage().getContent().getSubject(), Matchers.equalTo(createBlogDto.getSubject()));
        MatcherAssert.assertThat(responseEvent.getBody().getMessage().getContent().getText(), Matchers.equalTo(createBlogDto.getText()));
        MatcherAssert.assertThat(responseEvent.getBody().getAttributes(), Matchers.equalTo(createBlogDto.asMap()));
    }

    @Test
    void testCreateEventEvent() {
        CreateEventDto createEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/event_body.json", CreateEventDto.class);

        Event responseEvent = RestAssuredMockMvc
                .given()
                .config(RestAssuredMockMvcConfig.config().encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8")))
                .contentType(ContentType.JSON)
                .body(createEventDto)
                .when()
                .post("/events/create/event")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .status(HttpStatus.CREATED)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("koderia/event_schema/event_event_schema.json"))
                .extract().as(Event.class);

        MatcherAssert.assertThat(responseEvent.getBody().getMessage().getContent().getSubject(), Matchers.equalTo(createEventDto.getSubject()));
        MatcherAssert.assertThat(responseEvent.getBody().getMessage().getContent().getText(), Matchers.equalTo(createEventDto.getText()));
        MatcherAssert.assertThat(responseEvent.getBody().getAttributes(), Matchers.equalTo(createEventDto.asMap()));
    }

    @Test
    void testCreateLinkEvent() {
        CreateLinkDto createLinkDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/link_body.json", CreateLinkDto.class);

        Event responseEvent = RestAssuredMockMvc
                .given()
                .config(RestAssuredMockMvcConfig.config().encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8")))
                .contentType(ContentType.JSON)
                .body(createLinkDto)
                .when()
                .post("/events/create/link")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .status(HttpStatus.CREATED)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("koderia/event_schema/link_event_schema.json"))
                .extract().as(Event.class);

        MatcherAssert.assertThat(responseEvent.getBody().getMessage().getContent().getSubject(), Matchers.equalTo(createLinkDto.getSubject()));
        MatcherAssert.assertThat(responseEvent.getBody().getMessage().getContent().getText(), Matchers.equalTo(createLinkDto.getText()));
        MatcherAssert.assertThat(responseEvent.getBody().getAttributes(), Matchers.equalTo(createLinkDto.asMap()));
    }

    @Test
    void testCreateNewsEvent() {
        CreateNewsDto createNewsDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/news_body.json", CreateNewsDto.class);

        Event responseEvent = RestAssuredMockMvc
                .given()
                .config(RestAssuredMockMvcConfig.config().encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8")))
                .contentType(ContentType.JSON)
                .body(createNewsDto)
                .when()
                .post("/events/create/news")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .status(HttpStatus.CREATED)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("koderia/event_schema/news_event_schema.json"))
                .extract().as(Event.class);

        MatcherAssert.assertThat(responseEvent.getBody().getMessage().getContent().getSubject(), Matchers.equalTo(createNewsDto.getSubject()));
        MatcherAssert.assertThat(responseEvent.getBody().getMessage().getContent().getText(), Matchers.equalTo(createNewsDto.getText()));
        MatcherAssert.assertThat(responseEvent.getBody().getAttributes(), Matchers.equalTo(createNewsDto.asMap()));
    }

    @Test
    void testInvalidCreateJobPostEventFail() {
        Object createJobPostDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body_no_text.json", Object.class);

        RestAssuredMockMvc
                .given()
                    .config(RestAssuredMockMvcConfig.config().encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8")))
                    .contentType(ContentType.JSON)
                    .body(createJobPostDto)
                .when()
                    .post("/events/create/jobPost")
                .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .status(HttpStatus.BAD_REQUEST);
    }

}