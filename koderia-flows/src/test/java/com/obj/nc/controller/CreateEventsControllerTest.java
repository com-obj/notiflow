package com.obj.nc.controller;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.obj.nc.dto.CreateJobPostDto;
import com.obj.nc.utils.JsonUtils;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;

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
    void testCreateJobPostEventMatchesEventSchema() throws IOException, ProcessingException {
        CreateJobPostDto createJobPostEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", CreateJobPostDto.class);

        System.out.println(createJobPostEventDto);

        RestAssuredMockMvc
                .given()
                    .config(RestAssuredMockMvcConfig.config().encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8")))
                    .contentType(ContentType.JSON)
                    .body(createJobPostEventDto)
                .when()
                    .post("/events/create/jobPost")
                .then()
                    .log().ifValidationFails()
                    .assertThat()
                    .status(HttpStatus.CREATED)
                    .contentType(ContentType.JSON)
                    .body(matchesJsonSchemaInClasspath("koderia/event_schema/job_event_schema.json"));
    }

}