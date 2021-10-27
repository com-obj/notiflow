package com.obj.nc.functions.processors.senders.slack;

import com.obj.nc.domain.content.slack.SlackMessageContent;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.SlackEndpoint;
import com.obj.nc.domain.message.SlackMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static com.obj.nc.functions.processors.senders.slack.SlackMessageSenderConfig.SLACK_REST_TEMPLATE;

@Slf4j
@Component
public class SlackMessageSender extends ProcessorFunctionAdapter<SlackMessage, SlackMessage> {
    private final RestTemplate restTemplate;

    public SlackMessageSender(@Qualifier(SLACK_REST_TEMPLATE) RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    protected Optional<PayloadValidationException> checkPreCondition(SlackMessage payload) {
        Optional<PayloadValidationException> exception = super.checkPreCondition(payload);

        if (exception.isPresent()) {
            return exception;
        }

        List<? extends ReceivingEndpoint> endpoints = payload.getReceivingEndpoints();

        if (endpoints.size() != 1) {
            return Optional.of(new PayloadValidationException("SlackMessageSender can send to only one channel. Found more: " + endpoints));
        }

        ReceivingEndpoint endpoint = endpoints.get(0);

        if (!(endpoint instanceof SlackEndpoint)) {
            return Optional.of(new PayloadValidationException("SlackMessageSender can send to SlackEndpoint endpoints only. Found " + endpoint));
        }

        return Optional.empty();
    }

    @Override
    protected SlackMessage execute(SlackMessage payload) {
        HttpEntity<MultiValueMap<String, String>> request = prepareRequest(payload);
        verifyResponse(restTemplate.postForEntity("/chat.postMessage", request, SlackResponse.class));

        return payload;
    }

    private HttpEntity<MultiValueMap<String, String>> prepareRequest(SlackMessage payload) {
        SlackMessageContent content = payload.getBody();
        List<? extends ReceivingEndpoint> endpoints = payload.getReceivingEndpoints();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("channel", endpoints.get(0).getEndpointId());
        map.add("text", content.getText());

        return new HttpEntity<>(map, headers);
    }

    private void verifyResponse(ResponseEntity<SlackResponse> response) {
        SlackResponse slackResponse = response.getBody();
        final int statusCodeValue = response.getStatusCodeValue();

        if (statusCodeValue != 200 || slackResponse == null) {
            log.error("Failed to send slack message. Status code: " + statusCodeValue);
            throw new RuntimeException("Error sending Slack message.");
        }

        if (!slackResponse.isOk()) {
            String message = String.format("Error sending Slack message. Reason: %s.", slackResponse.getError());
            throw new RuntimeException(message);
        }
    }
}
