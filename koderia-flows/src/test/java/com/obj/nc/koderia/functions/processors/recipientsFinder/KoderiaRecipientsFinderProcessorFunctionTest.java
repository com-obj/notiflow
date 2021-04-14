package com.obj.nc.koderia.functions.processors.recipientsFinder;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.koderia.dto.koderia.data.JobPostEventDataDto;
import com.obj.nc.koderia.dto.koderia.data.RecipientDto;
import com.obj.nc.koderia.dto.koderia.data.RecipientsQueryDto;
import com.obj.nc.koderia.dto.koderia.event.JobPostKoderiaEventDto;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static com.obj.nc.functions.processors.eventFactory.GenericEventToNotificaitonIntentConverter.ORIGINAL_EVENT_FIELD;
import static com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinderConfig.RECIPIENTS_PATH;
import static java.util.stream.Collectors.toList;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@RestClientTest(KoderiaRecipientsFinderProcessorFunction.class)
@Import(KoderiaRecipientsFinderProcessorFunctionTestConfig.class)
class KoderiaRecipientsFinderProcessorFunctionTest {

    public static final String TEST_FILES_DIR_PATH = "koderia/recipient_queries/";

    @Autowired
    private KoderiaRecipientsFinderProcessorFunction getKoderiaRecipients;
    @Autowired
    private KoderiaRecipientsFinderConfig koderiaRecipientsFinderConfig;
    @Autowired
    private MockRestServiceServer mockServer;

    @ParameterizedTest
    @CsvSource({
            "job_event.json,job_recipients_query.json,job_recipients_response.json",
            "blog_event.json,blog_recipients_query.json,blog_recipients_response.json",
            "event_event.json,event_recipients_query.json,event_recipients_response.json",
            "link_event.json,link_recipients_query.json,link_recipients_response.json",
            "news_event.json,news_recipients_query.json,news_recipients_response.json"
    })
    void testFindEventRecipients(String inputEventFile, String expectedQueryPath, String expectedRecipientsPath) {
        // WITH MOCK SERVER
        RecipientsQueryDto queryDto = JsonUtils.readObjectFromClassPathResource(TEST_FILES_DIR_PATH + expectedQueryPath, RecipientsQueryDto.class);
        String queryDtoJsonString = JsonUtils.writeObjectToJSONString(queryDto);

        RecipientDto[] responseDto = JsonUtils.readObjectFromClassPathResource(TEST_FILES_DIR_PATH + expectedRecipientsPath, RecipientDto[].class);
        String responseDtoJsonString = JsonUtils.writeObjectToJSONString(responseDto);

        mockServer.expect(once(), requestTo(RECIPIENTS_PATH))
                .andExpect(content().json(queryDtoJsonString))
                .andRespond(withSuccess(responseDtoJsonString, MediaType.APPLICATION_JSON));

        // GIVEN
        NotificationIntent inputNotificationIntent = JsonUtils.readObjectFromClassPathResource(TEST_FILES_DIR_PATH + inputEventFile, NotificationIntent.class);

        // WHEN
        NotificationIntent outputNotificationIntent = getKoderiaRecipients.apply(inputNotificationIntent);

        // THEN
        List<String> recipientEmails = outputNotificationIntent.getBody().getRecievingEndpoints().stream().map(endpoint -> ((EmailEndpoint) endpoint).getEmail()).collect(toList());
        for (int i = 0; i < recipientEmails.size(); i++) {
            MatcherAssert.assertThat(recipientEmails.get(i), Matchers.equalTo(responseDto[i].getEmail()));
        }
    }

    @Test
    void testEventWithNoOriginalEvent() {
        // GIVEN
        NotificationIntent inputNotificationIntent = JsonUtils.readObjectFromClassPathResource(TEST_FILES_DIR_PATH + "job_event.json", NotificationIntent.class);
        inputNotificationIntent.getBody().removeAttribute(ORIGINAL_EVENT_FIELD);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> getKoderiaRecipients.apply(inputNotificationIntent))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("does not contain required attributes.")
                .hasMessageContaining(ORIGINAL_EVENT_FIELD);
    }

    @Test
    void testFindReceivingEndpoints() throws URISyntaxException {
        // given
        String QUERY_JSON_PATH = "koderia/recipient_queries/job_recipients_query.json";
        RecipientsQueryDto recipientsQueryDto = JsonUtils.readObjectFromClassPathResource(QUERY_JSON_PATH, RecipientsQueryDto.class);
        
        String RECIPIENTS_JSON_PATH = "koderia/recipient_queries/job_recipients_response.json";
        RecipientDto[] responseBody = JsonUtils.readObjectFromClassPathResource(RECIPIENTS_JSON_PATH, RecipientDto[].class);
        
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(RECIPIENTS_PATH)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + koderiaRecipientsFinderConfig.getKoderiaApiToken()))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JsonUtils.writeObjectToJSONString(responseBody))
                );
        
        // when
        List<RecievingEndpoint> outputRecipients = getKoderiaRecipients.findReceivingEndpoints(recipientsQueryDto);
        
        // then
        mockServer.verify();
        Assertions.assertThat(outputRecipients).isNotNull();
        Assertions.assertThat(outputRecipients).hasSize(3);
        
        Assertions.assertThat(((EmailEndpoint) outputRecipients.get(0)).getEmail()).isEqualTo(responseBody[0].getEmail());
        Assertions.assertThat(outputRecipients.get(0).getRecipient().getName()).isEqualTo(responseBody[0].getFirstName() + " " + responseBody[0].getLastName());
        
        Assertions.assertThat(((EmailEndpoint) outputRecipients.get(1)).getEmail()).isEqualTo(responseBody[1].getEmail());
        Assertions.assertThat(outputRecipients.get(1).getRecipient().getName()).isEqualTo(responseBody[1].getFirstName() + " " + responseBody[1].getLastName());
        
        Assertions.assertThat(((EmailEndpoint) outputRecipients.get(2)).getEmail()).isEqualTo(responseBody[2].getEmail());
        Assertions.assertThat(outputRecipients.get(2).getRecipient().getName()).isEqualTo(responseBody[2].getFirstName() + " " + responseBody[2].getLastName());
    }
    
    @Test
    void testFindReceivingEndpointsNullFail() throws URISyntaxException {
        // given
        String QUERY_JSON_PATH = "koderia/recipient_queries/job_recipients_query.json";
        RecipientsQueryDto recipientsQueryDto = JsonUtils.readObjectFromClassPathResource(QUERY_JSON_PATH, RecipientsQueryDto.class);
        
        RecipientDto[] responseBody = null;
        
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(RECIPIENTS_PATH)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + koderiaRecipientsFinderConfig.getKoderiaApiToken()))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JsonUtils.writeObjectToJSONString(responseBody))
                );
        
        // when - then
        Assertions.assertThatThrownBy(() -> getKoderiaRecipients.findReceivingEndpoints(recipientsQueryDto))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("Response body is null");
    }
    
    @Test
    void testFindReceivingEndpoints404HandleError() throws URISyntaxException {
        // given
        String QUERY_JSON_PATH = "koderia/recipient_queries/job_recipients_query.json";
        RecipientsQueryDto recipientsQueryDto = JsonUtils.readObjectFromClassPathResource(QUERY_JSON_PATH, RecipientsQueryDto.class);
        
        RecipientDto[] responseBody = null;
        
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(RECIPIENTS_PATH)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + koderiaRecipientsFinderConfig.getKoderiaApiToken()))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JsonUtils.writeObjectToJSONString(responseBody))
                );
        
        // when - then
        Assertions.assertThatThrownBy(() -> getKoderiaRecipients.findReceivingEndpoints(recipientsQueryDto))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("404");
    }
    
    @Test
    void testFindReceivingEndpoints500HandleError() throws URISyntaxException {
        // given
        String QUERY_JSON_PATH = "koderia/recipient_queries/job_recipients_query.json";
        RecipientsQueryDto recipientsQueryDto = JsonUtils.readObjectFromClassPathResource(QUERY_JSON_PATH, RecipientsQueryDto.class);
        
        RecipientDto[] responseBody = null;
        
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(RECIPIENTS_PATH)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + koderiaRecipientsFinderConfig.getKoderiaApiToken()))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JsonUtils.writeObjectToJSONString(responseBody))
                );
        
        // when - then
        Assertions.assertThatThrownBy(() -> getKoderiaRecipients.findReceivingEndpoints(recipientsQueryDto))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("500");
    }

}