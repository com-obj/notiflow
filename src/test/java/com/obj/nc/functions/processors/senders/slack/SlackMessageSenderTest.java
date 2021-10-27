package com.obj.nc.functions.processors.senders.slack;

import com.obj.nc.domain.content.slack.SlackMessageContent;
import com.obj.nc.domain.endpoints.SlackEndpoint;
import com.obj.nc.domain.message.SlackMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class SlackMessageSenderTest {
    @Test
    void testCall() {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        SlackResponse response = new SlackResponse();
        response.setOk(true);
        Mockito.when(restTemplate.postForEntity(eq("/chat.postMessage"), any(), eq(SlackResponse.class))).thenReturn(ResponseEntity.ok(response));

        SlackMessage message = new SlackMessage();
        SlackMessageContent content = SlackMessageContent.builder().text("Greetings").build();
        message.setBody(content);
        SlackEndpoint endpoint = SlackEndpoint.builder().channel("public-channel").build();
        message.setReceivingEndpoints(Collections.singletonList(endpoint));

        new SlackMessageSender(restTemplate).execute(message);

        verifyRequest(restTemplate, content, endpoint);
    }

    private void verifyRequest(RestTemplate restTemplate, SlackMessageContent content, SlackEndpoint endpoint) {
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        Mockito.verify(restTemplate).postForEntity(eq("/chat.postMessage"), captor.capture(), eq(SlackResponse.class));

        HttpEntity value = captor.getValue();
        Assertions.assertNotNull(value);

        MultiValueMap<String, String> map = (MultiValueMap<String, String>) value.getBody();
        Assertions.assertNotNull(map);
        Assertions.assertEquals(endpoint.getEndpointId(), map.get("channel").get(0));
        Assertions.assertEquals(content.getText(), map.get("text").get(0));
    }

    @Test
    void testResponseWithNot200HttpStatus() {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        Mockito.when(restTemplate.postForEntity(eq("/chat.postMessage"), any(), eq(SlackResponse.class))).thenReturn(ResponseEntity.notFound().build());

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> new SlackMessageSender(restTemplate).execute(prepareRequest()));
        Assertions.assertEquals("Error sending Slack message.", thrown.getMessage());
    }

    @Test
    void testResponseWithError() {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);

        SlackResponse response = new SlackResponse();
        response.setOk(false);
        response.setError("Channel does not exist");

        Mockito.when(restTemplate.postForEntity(eq("/chat.postMessage"), any(), eq(SlackResponse.class))).thenReturn(ResponseEntity.ok(response));

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> new SlackMessageSender(restTemplate).execute(prepareRequest()));
        Assertions.assertEquals("Error sending Slack message. Reason: " + response.getError() + ".", thrown.getMessage());
    }

    private SlackMessage prepareRequest() {
        SlackMessage message = new SlackMessage();
        SlackMessageContent content = SlackMessageContent.builder().text("Greetings").build();
        message.setBody(content);
        SlackEndpoint endpoint = SlackEndpoint.builder().channel("public-channel").build();
        message.setReceivingEndpoints(Collections.singletonList(endpoint));
        return message;
    }
}
