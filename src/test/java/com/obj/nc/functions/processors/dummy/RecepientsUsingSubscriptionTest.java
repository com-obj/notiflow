package com.obj.nc.functions.processors.dummy;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;

import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.processors.eventIdGenerator.GenerateEventIdProcessingFunction;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
class RecepientsUsingSubscriptionTest {

    private final GenerateEventIdProcessingFunction validateAndGenerateEventId =
            new GenerateEventIdProcessingFunction();

    private final DummyRecepientsEnrichmentProcessingFunction resolveRecipients =
            new DummyRecepientsEnrichmentProcessingFunction();

    @Test
    void testResolveRecipientsPassWithRequiredAttributes() {
        // given
        String INPUT_JSON_FILE = "intents/ba_job_post.json";
        NotificationIntent inputNotificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
        inputNotificationIntent = (NotificationIntent)validateAndGenerateEventId.apply(inputNotificationIntent);

        // when
        NotificationIntent outputNotificationIntent = (NotificationIntent)resolveRecipients.apply(inputNotificationIntent);

        // then
        MatcherAssert.assertThat(outputNotificationIntent, Matchers.notNullValue());
    }

    @Test
    void testResolveRecipientsResolvesAll() {
        // given
        String INPUT_JSON_FILE = "intents/ba_job_post.json";
        NotificationIntent inputNotificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
        inputNotificationIntent = (NotificationIntent)validateAndGenerateEventId.apply(inputNotificationIntent);

        // when
        NotificationIntent outputNotificationIntent = (NotificationIntent)resolveRecipients.apply(inputNotificationIntent);

        // then
        MatcherAssert.assertThat(outputNotificationIntent, Matchers.notNullValue());

        List<? extends RecievingEndpoint> outputEventEndpoints = outputNotificationIntent.getRecievingEndpoints();
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
        String INPUT_JSON_FILE = "intents/ba_job_post_recipients.json";
        NotificationIntent inputNotificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
        inputNotificationIntent = (NotificationIntent)validateAndGenerateEventId.apply(inputNotificationIntent);

        // when
        NotificationIntent outputNotificationIntent = (NotificationIntent)resolveRecipients.apply(inputNotificationIntent);

        // then
        List<? extends RecievingEndpoint> outputEventEndpoints = outputNotificationIntent.getRecievingEndpoints();
        MatcherAssert.assertThat(outputEventEndpoints, Matchers.hasSize(6));
        MatcherAssert.assertThat(outputEventEndpoints, Matchers.everyItem(Matchers.instanceOf(EmailEndpoint.class)));
    }

}