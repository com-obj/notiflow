package com.obj.nc.functions.processors.koderia;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.dummy.DummyRecepientsEnrichmentExecution;
import com.obj.nc.functions.processors.dummy.DummyRecepientsEnrichmentPreCondition;
import com.obj.nc.functions.processors.dummy.DummyRecepientsEnrichmentProcessingFunction;
import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdExecution;
import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdPreCondition;
import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdProcessingFunction;
import com.obj.nc.utils.JsonUtils;


class RecepientsUsingKoderiaSubscriptionTest {

    private final ValidateAndGenerateEventIdProcessingFunction validateAndGenerateEventId =
            new ValidateAndGenerateEventIdProcessingFunction(
                    new ValidateAndGenerateEventIdExecution(),
                    new ValidateAndGenerateEventIdPreCondition());

    private final DummyRecepientsEnrichmentProcessingFunction resolveRecipients =
            new DummyRecepientsEnrichmentProcessingFunction(
                    new DummyRecepientsEnrichmentExecution(),
                    new DummyRecepientsEnrichmentPreCondition());

    @Test
    void testResolveRecipientsFailWithoutRequiredAttributes() {
        // given
        NotificationIntent inputNotificationIntent = NotificationIntent.createWithSimpleMessage("test-config", "Hi there!!");

        // when - then
        Assertions.assertThatThrownBy(() -> resolveRecipients.apply(inputNotificationIntent))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("does not contain required attributes. Required attributes are:");
    }

    @Test
    void testResolveRecipientsPassWithRequiredAttributes() {
        // given
        String INPUT_JSON_FILE = "events/ba_job_post.json";
        NotificationIntent inputNotificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
        inputNotificationIntent = validateAndGenerateEventId.apply(inputNotificationIntent);

        // when
        NotificationIntent outputNotificationIntent = resolveRecipients.apply(inputNotificationIntent);

        // then
        MatcherAssert.assertThat(outputNotificationIntent, Matchers.notNullValue());
    }

    @Test
    void testResolveRecipientsResolvesAll() {
        // given
        String INPUT_JSON_FILE = "events/ba_job_post.json";
        NotificationIntent inputNotificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
        inputNotificationIntent = validateAndGenerateEventId.apply(inputNotificationIntent);

        // when
        NotificationIntent outputNotificationIntent = resolveRecipients.apply(inputNotificationIntent);

        // then
        MatcherAssert.assertThat(outputNotificationIntent, Matchers.notNullValue());

        List<RecievingEndpoint> outputEventEndpoints = outputNotificationIntent.getBody().getRecievingEndpoints();
        MatcherAssert.assertThat(outputEventEndpoints, Matchers.hasSize(3));
        MatcherAssert.assertThat(outputEventEndpoints, Matchers.everyItem(Matchers.instanceOf(EmailEndpoint.class)));

        MatcherAssert.assertThat(outputEventEndpoints.get(0).getRecipient().getName(), Matchers.equalTo("John Doe"));
        MatcherAssert.assertThat(((EmailEndpoint) outputEventEndpoints.get(0)).getEmail(), Matchers.equalTo("john.doe@objectify.sk"));

        MatcherAssert.assertThat(outputEventEndpoints.get(1).getRecipient().getName(), Matchers.equalTo("John Dudly"));
        MatcherAssert.assertThat(((EmailEndpoint) outputEventEndpoints.get(1)).getEmail(), Matchers.equalTo("john.dudly@objectify.sk"));

        MatcherAssert.assertThat(outputEventEndpoints.get(2).getRecipient().getName(), Matchers.equalTo("All Objectify"));
        MatcherAssert.assertThat(((EmailEndpoint) outputEventEndpoints.get(2)).getEmail(), Matchers.equalTo("all@objectify.sk"));
    }

    @Test
    void testResolveRecipientsMergeWithExisting() {
        // given
        String INPUT_JSON_FILE = "events/ba_job_post_recipients.json";
        NotificationIntent inputNotificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
        inputNotificationIntent = validateAndGenerateEventId.apply(inputNotificationIntent);

        // when
        NotificationIntent outputNotificationIntent = resolveRecipients.apply(inputNotificationIntent);

        // then
        List<RecievingEndpoint> outputEventEndpoints = outputNotificationIntent.getBody().getRecievingEndpoints();
        MatcherAssert.assertThat(outputEventEndpoints, Matchers.hasSize(6));
        MatcherAssert.assertThat(outputEventEndpoints, Matchers.everyItem(Matchers.instanceOf(EmailEndpoint.class)));
    }

}