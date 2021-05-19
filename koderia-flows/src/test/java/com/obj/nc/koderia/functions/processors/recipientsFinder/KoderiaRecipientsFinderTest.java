package com.obj.nc.koderia.functions.processors.recipientsFinder;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.koderia.domain.event.BaseKoderiaEvent;
import com.obj.nc.koderia.domain.recipients.RecipientDto;
import com.obj.nc.koderia.domain.recipients.RecipientsQueryDto;
import com.obj.nc.functions.processors.eventFactory.MailchimpEventConverter;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinderConfig.RECIPIENTS_PATH;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@RestClientTest(KoderiaRecipientsFinder.class)
@Import(KoderiaRecipientsFinderProcessorFunctionTestConfig.class)
class KoderiaRecipientsFinderTest {

    public static final String TEST_FILES_DIR_PATH = "koderia/recipient_queries/";
    public static final String TEST_BODIES = "koderia/create_request/";

    @Autowired private KoderiaRecipientsFinder getKoderiaRecipients;
    @Autowired private MailchimpEventConverter mailchimpEventConverter;
    @Autowired private KoderiaRecipientsFinderConfig koderiaRecipientsFinderConfig;
    @Autowired private MockRestServiceServer mockServer;
    
    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.bindTo(getKoderiaRecipients.getRestTemplate()).build();
    }
    
    @ParameterizedTest
    @CsvSource({
            "job_body.json,job_recipients_query.json,job_recipients_response.json",
            "blog_body.json,blog_recipients_query.json,blog_recipients_response.json",
            "event_body.json,event_recipients_query.json,event_recipients_response.json",
            "link_body.json,link_recipients_query.json,link_recipients_response.json",
            "news_body.json,news_recipients_query.json,news_recipients_response.json"
    })
    void testFindEventRecipients(String inputEventFile, String expectedQueryPath, String expectedRecipientsPath) {
        // WITH MOCK SERVER
        RecipientsQueryDto queryDto = JsonUtils.readObjectFromClassPathResource(TEST_FILES_DIR_PATH + expectedQueryPath, RecipientsQueryDto.class);
        String queryDtoJsonString = JsonUtils.writeObjectToJSONString(queryDto);

        RecipientDto[] responseDto = JsonUtils.readObjectFromClassPathResource(TEST_FILES_DIR_PATH + expectedRecipientsPath, RecipientDto[].class);
        String responseDtoJsonString = JsonUtils.writeObjectToJSONString(responseDto);

        mockServer.expect(once(), requestTo(koderiaRecipientsFinderConfig.getKoderiaApiUrl() + RECIPIENTS_PATH))
                .andExpect(content().json(queryDtoJsonString))
                .andRespond(withSuccess(responseDtoJsonString, MediaType.APPLICATION_JSON));

        // GIVEN
        BaseKoderiaEvent baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource(TEST_BODIES + inputEventFile, BaseKoderiaEvent.class);
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEvent));
        NotificationIntent inputNotificationIntent = mailchimpEventConverter.apply(genericEvent);

        // WHEN
        NotificationIntent outputNotificationIntent = getKoderiaRecipients.apply(inputNotificationIntent);

        // THEN
        List<String> recipientEmails = outputNotificationIntent.getBody().getRecievingEndpoints().stream().map(endpoint -> ((MailchimpEndpoint) endpoint).getEmail()).collect(toList());
        for (int i = 0; i < recipientEmails.size(); i++) {
            MatcherAssert.assertThat(recipientEmails.get(i), Matchers.equalTo(responseDto[i].getEmail()));
        }
    }

    @Test
    void testEventWithNoOriginalEvent() {
        // GIVEN
        BaseKoderiaEvent baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", BaseKoderiaEvent.class);
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEvent));
        NotificationIntent mappedNotificationIntent = mailchimpEventConverter.apply(genericEvent);
        MailchimpContent contentTyped = mappedNotificationIntent.getContentTyped();
        contentTyped.setOriginalEvent(null);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> getKoderiaRecipients.apply(mappedNotificationIntent))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("does not contain original event data");
    }

    @Test
    void testFindReceivingEndpoints() {
        // given
        String QUERY_JSON_PATH = "koderia/recipient_queries/job_recipients_query.json";
        RecipientsQueryDto recipientsQueryDto = JsonUtils.readObjectFromClassPathResource(QUERY_JSON_PATH, RecipientsQueryDto.class);
        
        String RECIPIENTS_JSON_PATH = "koderia/recipient_queries/job_recipients_response.json";
        RecipientDto[] responseBody = JsonUtils.readObjectFromClassPathResource(RECIPIENTS_JSON_PATH, RecipientDto[].class);
    
        createMockServerExpectations(responseBody, HttpStatus.OK);
    
        // when
        List<RecievingEndpoint> outputRecipients = getKoderiaRecipients.requestReceivingEndpoints(recipientsQueryDto);
        
        // then
        mockServer.verify();
        Assertions.assertThat(outputRecipients).isNotNull();
        Assertions.assertThat(outputRecipients).hasSize(3);
        
        Assertions.assertThat(((MailchimpEndpoint) outputRecipients.get(0)).getEmail()).isEqualTo(responseBody[0].getEmail());
        Assertions.assertThat(outputRecipients.get(0).getRecipient().getName()).isEqualTo(responseBody[0].getFirstName() + " " + responseBody[0].getLastName());
        
        Assertions.assertThat(((MailchimpEndpoint) outputRecipients.get(1)).getEmail()).isEqualTo(responseBody[1].getEmail());
        Assertions.assertThat(outputRecipients.get(1).getRecipient().getName()).isEqualTo(responseBody[1].getFirstName() + " " + responseBody[1].getLastName());
        
        Assertions.assertThat(((MailchimpEndpoint) outputRecipients.get(2)).getEmail()).isEqualTo(responseBody[2].getEmail());
        Assertions.assertThat(outputRecipients.get(2).getRecipient().getName()).isEqualTo(responseBody[2].getFirstName() + " " + responseBody[2].getLastName());
    }

    
    @Test
    void testFindReceivingEndpointsNullFail() {
        // given
        String QUERY_JSON_PATH = "koderia/recipient_queries/job_recipients_query.json";
        RecipientsQueryDto recipientsQueryDto = JsonUtils.readObjectFromClassPathResource(QUERY_JSON_PATH, RecipientsQueryDto.class);
        
        RecipientDto[] responseBody = null;
    
        createMockServerExpectations(responseBody, HttpStatus.OK);
    
        // when - then
        Assertions.assertThatThrownBy(() -> getKoderiaRecipients.requestReceivingEndpoints(recipientsQueryDto))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("Response body is null");
    }
    
    @Test
    void testFindReceivingEndpoints404HandleError() {
        // given
        String QUERY_JSON_PATH = "koderia/recipient_queries/job_recipients_query.json";
        RecipientsQueryDto recipientsQueryDto = JsonUtils.readObjectFromClassPathResource(QUERY_JSON_PATH, RecipientsQueryDto.class);
        
        RecipientDto[] responseBody = null;
    
        createMockServerExpectations(responseBody, HttpStatus.NOT_FOUND);
    
        // when - then
        Assertions.assertThatThrownBy(() -> getKoderiaRecipients.requestReceivingEndpoints(recipientsQueryDto))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("404");
    }
    
    @Test
    void testFindReceivingEndpoints500HandleError() {
        // given
        String QUERY_JSON_PATH = "koderia/recipient_queries/job_recipients_query.json";
        RecipientsQueryDto recipientsQueryDto = JsonUtils.readObjectFromClassPathResource(QUERY_JSON_PATH, RecipientsQueryDto.class);
        
        RecipientDto[] responseBody = null;
    
        createMockServerExpectations(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    
        // when - then
        Assertions.assertThatThrownBy(() -> getKoderiaRecipients.requestReceivingEndpoints(recipientsQueryDto))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("500");
    }
    
    private void createMockServerExpectations(RecipientDto[] responseBody, HttpStatus httpStatus) {
        mockServer.expect(ExpectedCount.once(),
                requestTo(koderiaRecipientsFinderConfig.getKoderiaApiUrl() + RECIPIENTS_PATH))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + koderiaRecipientsFinderConfig.getKoderiaApiToken()))
                .andExpect(jsonPath("$.type", equalTo("JOB_POST")))
                .andExpect(jsonPath("$.data.type", equalTo("Analytik")))
                .andExpect(jsonPath("$.data.technologies[0]", equalTo("Microsoft Power BI")))
                .andRespond(withStatus(httpStatus)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JsonUtils.writeObjectToJSONString(responseBody))
                );
    }

}