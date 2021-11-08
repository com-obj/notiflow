package com.obj.nc.functions.processors.senders.slack;

import com.obj.nc.domain.endpoints.SlackEndpoint;
import com.obj.nc.domain.endpoints.push.PushEndpoint;
import com.obj.nc.domain.message.SlackMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SlackMessageSender_CheckPreConditionTest {
    @Test
    void testCheckPreCondition_NoEndpoints() {
        Optional<PayloadValidationException> exception = execCheckPreCondition(new SlackMessage());

        assertTrue(exception.isPresent());
        assertEquals("SlackMessageSender can send to only one channel. Found more: []", exception.get().getMessage());
    }

    @Test
    void testCheckPreCondition_MoreThenOneEndpoint() {
        SlackMessage message = new SlackMessage();
        message.setReceivingEndpoints(Arrays.asList(createSlackEndpoint(), createSlackEndpoint()));
        Optional<PayloadValidationException> exception = execCheckPreCondition(message);

        assertTrue(exception.isPresent());
        assertEquals("SlackMessageSender can send to only one channel. Found more: " + message.getReceivingEndpoints(), exception.get().getMessage());
    }

    @Test
    void testCheckPreCondition_InvalidEndpoint() {
        SlackMessage message = new SlackMessage();
        message.setReceivingEndpoints(Collections.singletonList(PushEndpoint.ofToken("token")));
        Optional<PayloadValidationException> exception = execCheckPreCondition(message);

        assertTrue(exception.isPresent());
        assertEquals("SlackMessageSender can send to SlackEndpoint endpoints only. Found DirectPushEndpoint(token=token)", exception.get().getMessage());
    }

    @Test
    void testCheckPreCondition() {
        SlackMessage message = new SlackMessage();
        message.setReceivingEndpoints(Collections.singletonList(createSlackEndpoint()));
        Optional<PayloadValidationException> exception = execCheckPreCondition(message);

        assertFalse(exception.isPresent());
    }

    private Optional<PayloadValidationException> execCheckPreCondition(SlackMessage message) {
        return new SlackMessageSender(null).checkPreCondition(message);
    }

    private SlackEndpoint createSlackEndpoint() {
        return SlackEndpoint.builder().channel("channel").build();
    }
}
