package com.obj.nc.functions.processors.senders.teams;

import com.obj.nc.domain.content.teams.TeamsMessageContent;
import com.obj.nc.domain.endpoints.TeamsEndpoint;
import com.obj.nc.domain.message.TeamsMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class TeamsMessageSenderTest {
    private static final String WEBHOOK_URL = "http://webhook.com/code";
    private static final String TEXT = "Greetings";

    @Test
    void testCall() {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        Mockito.when(restTemplate.postForEntity(eq(WEBHOOK_URL), any(), eq(String.class))).thenReturn(ResponseEntity.ok().build());

        TeamsMessage message = new TeamsMessage();
        message.setBody(TeamsMessageContent.builder().text(TEXT).build());
        TeamsEndpoint endpoint = TeamsEndpoint.builder().webhookUrl(WEBHOOK_URL).build();
        message.setReceivingEndpoints(Collections.singletonList(endpoint));

        new TeamsMessageSender(restTemplate).execute(message);

        verifyRequest(restTemplate);
    }

    private void verifyRequest(RestTemplate restTemplate) {
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        Mockito.verify(restTemplate).postForEntity(eq(WEBHOOK_URL), captor.capture(), eq(String.class));

        HttpEntity value = captor.getValue();
        Assertions.assertNotNull(value);
        Assertions.assertEquals(MediaType.APPLICATION_JSON, value.getHeaders().getContentType());

        String message = (String) value.getBody();
        Assertions.assertNotNull(message);
        Assertions.assertEquals("{\"text\":\"" + TEXT + "\"}", message);
    }

    @Test
    void testResponseWithNot200HttpStatus() {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        Mockito.when(restTemplate.postForEntity(eq(WEBHOOK_URL), any(), eq(String.class))).thenReturn(ResponseEntity.badRequest().body("Summary or Text is required."));

        TeamsMessage message = new TeamsMessage();
        message.setBody(TeamsMessageContent.builder().text("").build());
        TeamsEndpoint endpoint = TeamsEndpoint.builder().webhookUrl(WEBHOOK_URL).build();
        message.setReceivingEndpoints(Collections.singletonList(endpoint));

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> new TeamsMessageSender(restTemplate).execute(message));
        Assertions.assertEquals("Error sending teams message. Service responded with code 400 and Reason: Summary or Text is required.", thrown.getMessage());
    }
}
