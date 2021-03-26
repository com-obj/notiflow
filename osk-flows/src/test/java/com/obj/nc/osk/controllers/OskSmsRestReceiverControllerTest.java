package com.obj.nc.osk.controllers;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.message.Message;
import com.obj.nc.osk.dto.OskSendSmsRequestDto;
import com.obj.nc.osk.dto.OskSendSmsResponseDto;
import com.obj.nc.osk.functions.senders.OskSmsSenderConfigProperties;
import com.obj.nc.osk.services.OskSmsRestClientImpl;
import com.obj.nc.utils.JsonUtils;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

import static com.obj.nc.osk.services.OskSmsRestClientImpl.SEND_PATH;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext
class OskSmsRestReceiverControllerTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private OskSmsRestClientImpl restClient;
    
    @Autowired
    private OskSmsSenderConfigProperties properties;
    
    @BeforeEach
    public void initialiseRestAssuredMockMvcWebApplicationContext() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        RestAssured.baseURI = "http://localhost";
        String contextPath = webApplicationContext.getServletContext().getContextPath();
        RestAssured.basePath = contextPath;
    }
    
    @Test
    void testSendValidRequest() {
        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource("smsNotificationMessages/message.json", Message.class);
        OskSendSmsRequestDto oskSendSmsRequestDto = restClient.convertMessageToRequest(inputMessage);
    
        Response response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(oskSendSmsRequestDto)
                .when()
                .post(properties.getGapApiUrl() + SEND_PATH, oskSendSmsRequestDto.getSenderAddress())
                .then()
                .extract().response();
    
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        OskSendSmsResponseDto responseBodyDto = response.getBody().as(OskSendSmsResponseDto.class);
        Assertions.assertThat(responseBodyDto.getResourceReference().getResourceURL())
                .contains(oskSendSmsRequestDto.getAddress().get(0))
                .contains(oskSendSmsRequestDto.getSenderAddress())
                .contains("SUCCESS");
    }
    
    @Test
    void testSendInvalidRequest() {
        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource("smsNotificationMessages/message.json", Message.class);
        OskSendSmsRequestDto oskSendSmsRequestDto = restClient.convertMessageToRequest(inputMessage);
        oskSendSmsRequestDto.setAddress(null);
    
        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(oskSendSmsRequestDto)
                .when()
                .post(properties.getGapApiUrl() + SEND_PATH, oskSendSmsRequestDto.getSenderAddress())
                .then()
                .statusCode(400);
    }
    
}