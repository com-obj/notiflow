package com.obj.nc.koderia.functions.processors;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.koderia.dto.RecipientDto;
import com.obj.nc.koderia.dto.RecipientsQueryDto;
import com.obj.nc.koderia.functions.processors.KoderiaRecipientsProcessingFunction;
import com.obj.nc.koderia.services.KoderiaRestClientImpl;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.obj.nc.koderia.functions.processors.KoderiaEventConverterExecution.ORIGINAL_EVENT_FIELD;
import static com.obj.nc.koderia.services.KoderiaRestClientImpl.RECIPIENTS_PATH;
import static java.util.stream.Collectors.toList;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@RestClientTest(KoderiaRestClientImpl.class)
@Import(KoderiaRecipientsProcessingFunctionTestConfig.class)
class KoderiaRecipientsProcessingFunctionTest {

    public static final String TEST_FILES_DIR_PATH = "koderia/recipient_queries/";

    @Autowired
    private KoderiaRecipientsProcessingFunction getKoderiaRecipients;

    @Autowired
    private MockRestServiceServer server;

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

        server.expect(once(), requestTo(RECIPIENTS_PATH))
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
        inputNotificationIntent.getBody().getAttributes().remove(ORIGINAL_EVENT_FIELD);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> getKoderiaRecipients.apply(inputNotificationIntent))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("does not contain required attributes.")
                .hasMessageContaining(ORIGINAL_EVENT_FIELD);
    }

    @Test
    void testInconvertibleEvent() {
        // GIVEN
        NotificationIntent inputNotificationIntent = JsonUtils.readObjectFromClassPathResource(TEST_FILES_DIR_PATH + "job_event.json", NotificationIntent.class);
        ((Map<String, Object>) inputNotificationIntent.getBody().getAttributes().get("originalEvent")).put("type", "INVALID");

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> getKoderiaRecipients.apply(inputNotificationIntent))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Cannot deserialize value of");
    }

}