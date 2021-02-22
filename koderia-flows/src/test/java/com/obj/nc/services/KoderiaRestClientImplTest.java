package com.obj.nc.services;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.dto.CategorizedRecipientsQueryDto;
import com.obj.nc.dto.RecipientDto;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
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

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ActiveProfiles("test")
@RestClientTest(KoderiaRestClientImpl.class)
@Import(KoderiaServiceRestImplTestConfig.class)
class KoderiaRestClientImplTest {

    @Autowired
    private KoderiaRestClientImpl koderiaService;

    @Autowired
    private MockRestServiceServer mockServer;

    @Test
    void testFindReceivingEndpoints() throws URISyntaxException {
        // given
        String QUERY_JSON_PATH = "koderia/recipient_queries/job_recipients_query.json";
        CategorizedRecipientsQueryDto recipientsQueryDto = JsonUtils.readObjectFromClassPathResource(QUERY_JSON_PATH, CategorizedRecipientsQueryDto.class);

        String RECIPIENTS_JSON_PATH = "koderia/recipients.json";
        RecipientDto[] responseBody = JsonUtils.readObjectFromClassPathResource(RECIPIENTS_JSON_PATH, RecipientDto[].class);

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(KoderiaRestClientImpl.RECIPIENTS_PATH)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JsonUtils.writeObjectToJSONString(responseBody))
                );

        // when
        List<EmailEndpoint> outputRecipients = koderiaService.findEmailEndpoints(recipientsQueryDto);

        // then
        mockServer.verify();
        Assertions.assertThat(outputRecipients).isNotNull();
        Assertions.assertThat(outputRecipients).hasSize(2);

        Assertions.assertThat(outputRecipients.get(0).getEmail()).isEqualTo(responseBody[0].getEmail());
        Assertions.assertThat(outputRecipients.get(0).getRecipient().getName()).isEqualTo(responseBody[0].getFirstName() + " " + responseBody[0].getLastName());

        Assertions.assertThat(outputRecipients.get(1).getEmail()).isEqualTo(responseBody[1].getEmail());
        Assertions.assertThat(outputRecipients.get(1).getRecipient().getName()).isEqualTo(responseBody[1].getFirstName() + " " + responseBody[1].getLastName());
    }

    @Test
    void testFindReceivingEndpointsNullFail() throws URISyntaxException {
        // given
        String QUERY_JSON_PATH = "koderia/recipient_queries/job_recipients_query.json";
        CategorizedRecipientsQueryDto recipientsQueryDto = JsonUtils.readObjectFromClassPathResource(QUERY_JSON_PATH, CategorizedRecipientsQueryDto.class);

        RecipientDto[] responseBody = null;

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(KoderiaRestClientImpl.RECIPIENTS_PATH)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JsonUtils.writeObjectToJSONString(responseBody))
                );

        // when - then
        Assertions.assertThatThrownBy(() -> koderiaService.findEmailEndpoints(recipientsQueryDto))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("Response body is null");
    }

    @Test
    void testFindReceivingEndpoints404HandleError() throws URISyntaxException {
        // given
        String QUERY_JSON_PATH = "koderia/recipient_queries/job_recipients_query.json";
        CategorizedRecipientsQueryDto recipientsQueryDto = JsonUtils.readObjectFromClassPathResource(QUERY_JSON_PATH, CategorizedRecipientsQueryDto.class);

        RecipientDto[] responseBody = null;

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI( KoderiaRestClientImpl.RECIPIENTS_PATH)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JsonUtils.writeObjectToJSONString(responseBody))
                );

        // when - then
        Assertions.assertThatThrownBy(() -> koderiaService.findEmailEndpoints(recipientsQueryDto))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("404 NOT_FOUND");
    }

    @Test
    void testFindReceivingEndpoints500HandleError() throws URISyntaxException {
        // given
        String QUERY_JSON_PATH = "koderia/recipient_queries/job_recipients_query.json";
        CategorizedRecipientsQueryDto recipientsQueryDto = JsonUtils.readObjectFromClassPathResource(QUERY_JSON_PATH, CategorizedRecipientsQueryDto.class);

        RecipientDto[] responseBody = null;

        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI(KoderiaRestClientImpl.RECIPIENTS_PATH)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JsonUtils.writeObjectToJSONString(responseBody))
                );

        // when - then
        Assertions.assertThatThrownBy(() -> koderiaService.findEmailEndpoints(recipientsQueryDto))
                .isInstanceOf(RestClientException.class)
                .hasMessageContaining("500 INTERNAL_SERVER_ERROR");
    }

}