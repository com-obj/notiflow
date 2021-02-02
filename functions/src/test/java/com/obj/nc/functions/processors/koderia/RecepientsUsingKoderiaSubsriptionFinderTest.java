package com.obj.nc.functions.processors.koderia;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.event.Event;
import com.obj.nc.functions.processors.EventIdGenerator;
import com.obj.nc.utils.JsonUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;


class RecepientsUsingKoderiaSubsriptionFinderTest {

    @Test
    void testResolveRecipientsFailWithoutRequiredAttributes() {
        // given
        Event inputEvent = Event.createWithSimpleMessage("test-config", "Hi there!!");
        EventIdGenerator.ValidateAndGenerateEventId function = new EventIdGenerator.ValidateAndGenerateEventId();
        inputEvent = function.apply(inputEvent);

        // when
        RecepientsUsingKoderiaSubsriptionFinder.ResolveRecipients resolveRecipients = new RecepientsUsingKoderiaSubsriptionFinder.ResolveRecipients();
        Event outputEvent = resolveRecipients.apply(inputEvent);

        // then
        List<String> outputEventEndpoints = outputEvent.getBody().getRecievingEndpoints().stream().map(ep -> ep.getRecipient().getName()).collect(Collectors.toList());
        MatcherAssert.assertThat(outputEventEndpoints, Matchers.empty());
    }

    @Test
    void testResolveRecipientsPassWithRequiredAttributes() {
        // given
        String INPUT_JSON_FILE = "events/ba_job_post.json";
        Event inputEvent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Event.class);
        EventIdGenerator.ValidateAndGenerateEventId function = new EventIdGenerator.ValidateAndGenerateEventId();
        inputEvent = function.apply(inputEvent);

        // when
        RecepientsUsingKoderiaSubsriptionFinder.ResolveRecipients resolveRecipients = new RecepientsUsingKoderiaSubsriptionFinder.ResolveRecipients();
        Event outputEvent = resolveRecipients.apply(inputEvent);

        // then
        MatcherAssert.assertThat(outputEvent, Matchers.notNullValue());
    }

    @Test
    void testResolveRecipientsResolvesAll() {
        // given
        String INPUT_JSON_FILE = "events/ba_job_post.json";
        Event inputEvent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Event.class);
        EventIdGenerator.ValidateAndGenerateEventId function = new EventIdGenerator.ValidateAndGenerateEventId();
        inputEvent = function.apply(inputEvent);

        // when
        RecepientsUsingKoderiaSubsriptionFinder.ResolveRecipients resolveRecipients = new RecepientsUsingKoderiaSubsriptionFinder.ResolveRecipients();
        Event outputEvent = resolveRecipients.apply(inputEvent);

        // then
        MatcherAssert.assertThat(outputEvent, Matchers.notNullValue());

        List<RecievingEndpoint> outputEventEndpoints = outputEvent.getBody().getRecievingEndpoints();
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
        Event inputEvent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Event.class);
        EventIdGenerator.ValidateAndGenerateEventId function = new EventIdGenerator.ValidateAndGenerateEventId();
        inputEvent = function.apply(inputEvent);

        // when
        RecepientsUsingKoderiaSubsriptionFinder.ResolveRecipients resolveRecipients = new RecepientsUsingKoderiaSubsriptionFinder.ResolveRecipients();
        Event outputEvent = resolveRecipients.apply(inputEvent);

        // then
        List<RecievingEndpoint> outputEventEndpoints = outputEvent.getBody().getRecievingEndpoints();
        MatcherAssert.assertThat(outputEventEndpoints, Matchers.hasSize(6));
        MatcherAssert.assertThat(outputEventEndpoints, Matchers.everyItem(Matchers.instanceOf(EmailEndpoint.class)));
    }

}