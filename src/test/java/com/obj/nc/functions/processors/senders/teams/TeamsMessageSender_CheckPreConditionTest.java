package com.obj.nc.functions.processors.senders.teams;

import com.obj.nc.domain.content.teams.TeamsMessageContent;
import com.obj.nc.domain.endpoints.TeamsEndpoint;
import com.obj.nc.domain.endpoints.push.PushEndpoint;
import com.obj.nc.domain.message.TeamsMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TeamsMessageSender_CheckPreConditionTest {
    @Test
    void testCheckPreCondition_NoEndpoints() {
        TeamsMessage message = new TeamsMessage();
        message.setBody(createBody("text"));
        Optional<PayloadValidationException> exception = execCheckPreCondition(message);

        assertTrue(exception.isPresent());
        assertEquals("TeamsMessageSender can send to only one channel. Found more: []", exception.get().getMessage());
    }


    @Test
    void testCheckPreCondition_MoreThenOneEndpoint() {
        TeamsMessage message = new TeamsMessage();
        message.setBody(createBody("text"));
        message.setReceivingEndpoints(Arrays.asList(createTeamsEndpoint(), createTeamsEndpoint()));
        Optional<PayloadValidationException> exception = execCheckPreCondition(message);

        assertTrue(exception.isPresent());
        assertEquals("TeamsMessageSender can send to only one channel. Found more: " + message.getReceivingEndpoints(), exception.get().getMessage());
    }

    @Test
    void testCheckPreCondition_InvalidEndpoint() {
        TeamsMessage message = new TeamsMessage();
        message.setBody(createBody("text"));
        message.setReceivingEndpoints(Collections.singletonList(PushEndpoint.ofToken("token")));
        Optional<PayloadValidationException> exception = execCheckPreCondition(message);

        assertTrue(exception.isPresent());
        assertEquals("TeamsMessageSender can send to TeamsEndpoint endpoints only. Found DirectPushEndpoint(token=token)", exception.get().getMessage());
    }

    @Test
    @Disabled // TODO : message body is @NonNull -> cannot set it to null 
    void testCheckPreCondition_NoText() {
        TeamsMessage message = new TeamsMessage();
        message.setBody(createBody(null));
        message.setReceivingEndpoints(Collections.singletonList(createTeamsEndpoint()));
        Optional<PayloadValidationException> exception = execCheckPreCondition(message);

        assertTrue(exception.isPresent());
        assertEquals("TeamsMessageContent text cannot be empty", exception.get().getMessage());
    }

    @Test
    void testCheckPreCondition() {
        TeamsMessage message = new TeamsMessage();
        message.setBody(createBody("text"));
        message.setReceivingEndpoints(Collections.singletonList(createTeamsEndpoint()));
        Optional<PayloadValidationException> exception = execCheckPreCondition(message);

        assertFalse(exception.isPresent());
    }

    private TeamsMessageContent createBody(String text) {
        return TeamsMessageContent.builder().text(text).build();
    }

    private Optional<PayloadValidationException> execCheckPreCondition(TeamsMessage message) {
        return new TeamsMessageSender(null).checkPreCondition(message);
    }

    private TeamsEndpoint createTeamsEndpoint() {
        return TeamsEndpoint.builder().webhookUrl ("channel").build();
    }
}
