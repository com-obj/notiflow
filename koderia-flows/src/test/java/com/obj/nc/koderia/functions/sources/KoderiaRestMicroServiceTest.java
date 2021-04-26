package com.obj.nc.koderia.functions.sources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.WebApplicationContext;

import com.obj.nc.koderia.KoderiaFlowsApplication;
import com.obj.nc.koderia.controller.EmitEventsRestController;
import com.obj.nc.koderia.dto.EmitEventDto;
import com.obj.nc.utils.JsonUtils;

import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig;

@WebMvcTest(controllers = {
        EmitEventsRestController.class,
        KoderiaRestMicroService.class
})
@ContextConfiguration(classes = KoderiaFlowsApplication.class)
class KoderiaRestMicroServiceTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void initialiseRestAssuredMockMvcWebApplicationContext() {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
    }

    @Test
    void testEmitJobPostEvent() {
        EmitEventDto emitJobPostDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", EmitEventDto.class);

        RestAssuredMockMvc
                .given()
                .config(RestAssuredMockMvcConfig.config().encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8")))
                .contentType(ContentType.JSON)
                .body(emitJobPostDto)
                .when()
                .post("/events/emit")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .status(HttpStatus.OK);
    }

    @Test
    void testEmitBlogEvent() {
        EmitEventDto emitBlogDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/blog_body.json", EmitEventDto.class);

        RestAssuredMockMvc
                .given()
                .config(RestAssuredMockMvcConfig.config().encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8")))
                .contentType(ContentType.JSON)
                .body(emitBlogDto)
                .when()
                .post("/events/emit")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .status(HttpStatus.OK);
    }

    @Test
    void testEmitEventEvent() {
        EmitEventDto emitEventEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/event_body.json", EmitEventDto.class);

        RestAssuredMockMvc
                .given()
                .config(RestAssuredMockMvcConfig.config().encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8")))
                .contentType(ContentType.JSON)
                .body(emitEventEventDto)
                .when()
                .post("/events/emit")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .status(HttpStatus.OK);
    }

    @Test
    void testEmitLinkEvent() {
        EmitEventDto emitLinkDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/link_body.json", EmitEventDto.class);

        RestAssuredMockMvc
                .given()
                .config(RestAssuredMockMvcConfig.config().encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8")))
                .contentType(ContentType.JSON)
                .body(emitLinkDto)
                .when()
                .post("/events/emit")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .status(HttpStatus.OK);
    }

    @Test
    void testEmitNewsEvent() {
        EmitEventDto emitNewsDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/news_body.json", EmitEventDto.class);

        RestAssuredMockMvc
                .given()
                .config(RestAssuredMockMvcConfig.config().encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8")))
                .contentType(ContentType.JSON)
                .body(emitNewsDto)
                .when()
                .post("/events/emit")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .status(HttpStatus.OK);
    }

    @Test
    void testEmitInvalidJobPostEvent() {
        Object invalidEmitJobPostDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body_no_text.json", Object.class);

        RestAssuredMockMvc
                .given()
                .config(RestAssuredMockMvcConfig.config().encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8")))
                .contentType(ContentType.JSON)
                .body(invalidEmitJobPostDto)
                .when()
                .post("/events/emit")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .status(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testEmitJobPostAsBlog() {
        EmitEventDto blogBody = JsonUtils.readObjectFromClassPathResource("koderia/create_request/blog_body.json", EmitEventDto.class);
        blogBody.setType(EmitEventDto.Type.JOB_POST);

        RestAssuredMockMvc
                .given()
                .config(RestAssuredMockMvcConfig.config().encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8")))
                .contentType(ContentType.JSON)
                .body(blogBody)
                .when()
                .post("/events/emit")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .status(HttpStatus.BAD_REQUEST);
    }
  
}