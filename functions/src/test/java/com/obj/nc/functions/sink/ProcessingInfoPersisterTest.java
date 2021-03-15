package com.obj.nc.functions.sink;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.ProcessingInfo;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.koderia.DummyRecepientsEnrichmentProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromEventProcessingFunction;
import com.obj.nc.functions.processors.senders.EmailSenderSinkExecution;
import com.obj.nc.functions.processors.senders.EmailSenderSinkProcessingFunction;
import com.obj.nc.functions.sink.processingInfoPersister.ProcessingInfoPersisterSinkConsumer;
import com.obj.nc.functions.sink.processingInfoPersister.eventWithRecipients.ProcessingInfoPersisterForEventWithRecipientsSinkConsumer;
import com.obj.nc.utils.JsonUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
class ProcessingInfoPersisterTest extends BaseIntegrationTest {

    @Autowired
    private ProcessingInfoPersisterSinkConsumer processingInfoPersister;

    @Autowired
    private ProcessingInfoPersisterForEventWithRecipientsSinkConsumer processingInfoPersisterForEventWithRecipients;

    @Autowired
    private ValidateAndGenerateEventIdProcessingFunction validateAndGenerateEventId;

    @Autowired
    private DummyRecepientsEnrichmentProcessingFunction resolveRecipients;

    @Autowired
    private MessagesFromEventProcessingFunction generateMessagesFromEvent;

    @Autowired
    private EmailSenderSinkProcessingFunction functionSend;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("truncate table nc_processing_info");
        jdbcTemplate.execute("truncate table nc_endpoint");
    }

    @Test
    void testPersistPIForEvent() {
        // given
        Event event = Event.createWithSimpleMessage("test-config", "Hi there!!");
        event = validateAndGenerateEventId.apply(event);

        // when
        processingInfoPersister.accept(event);

        // then
        UUID uuid = event.getHeader().getId();
        final Event finalEvent = event;

        List<ProcessingInfo> persistedPIs = ProcessingInfo.findProcessingInfo(uuid, "GenerateMessagesFromEvent");
        persistedPIs.forEach(persistedPI -> {
            assertThat(persistedPI.getProcessingId(), CoreMatchers.equalTo(finalEvent.getProcessingInfo().getProcessingId()));
            assertThat(persistedPI.getPrevProcessingId(), CoreMatchers.equalTo(finalEvent.getProcessingInfo().getPrevProcessingId()));
            assertThat(persistedPI.getPrevProcessingId(), CoreMatchers.equalTo(finalEvent.getProcessingInfo().getPrevProcessingId()));
            assertThat(persistedPI.getPrevProcessingId(), CoreMatchers.equalTo(finalEvent.getProcessingInfo().getPrevProcessingId()));
            assertThat(persistedPI.getDiffJson(), CoreMatchers.equalTo(finalEvent.getProcessingInfo().getDiffJson()));
            assertThat(persistedPI.getStepName(), CoreMatchers.equalTo(finalEvent.getProcessingInfo().getStepName()));
            assertThat(persistedPI.getEventJson(), CoreMatchers.equalTo(finalEvent.getProcessingInfo().getEventJson()));
            assertThat(persistedPI.getDurationInMs(), CoreMatchers.equalTo(finalEvent.getProcessingInfo().getDurationInMs()));
            assertThat(persistedPI.getTimeStampStart(), CoreMatchers.equalTo(finalEvent.getProcessingInfo().getTimeStampStart()));
            assertThat(persistedPI.getTimeStampFinish(), CoreMatchers.equalTo(finalEvent.getProcessingInfo().getTimeStampFinish()));
        });
    }

    @Test
    void testPersistPIForEventWithRecipients() {
        // given
        String INPUT_JSON_FILE = "events/ba_job_post.json";
        Event event = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Event.class);
        event = validateAndGenerateEventId.apply(event);
        event = resolveRecipients.apply(event);

        // when
        processingInfoPersisterForEventWithRecipients.accept(event);

        // then
        List<Map<String, Object>> persistedEndpoints = jdbcTemplate.queryForList("select * from nc_endpoint");
        assertThat(persistedEndpoints, CoreMatchers.notNullValue());

        for (int i = 0; i < persistedEndpoints.size(); i++) {
            List<RecievingEndpoint> recievingEndpoints = event.getBody().getRecievingEndpoints();
            assertThat(persistedEndpoints.get(i).get("endpoint_name"), CoreMatchers.equalTo(((EmailEndpoint) recievingEndpoints.get(i)).getEmail()));
            assertThat(persistedEndpoints.get(i).get("endpoint_type"), CoreMatchers.equalTo(recievingEndpoints.get(i).getEndpointTypeName()));
        }
    }

    @Test
    void testPersistPIForMessage() {
        // given
        String INPUT_JSON_FILE = "events/ba_job_post.json";
        Event event = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Event.class);
        event = validateAndGenerateEventId.apply(event);
        event = resolveRecipients.apply(event);
        List<Message> messages = generateMessagesFromEvent.apply(event);

        // when
        messages.forEach(message -> processingInfoPersister.accept(message));

        // then
        messages.forEach(message -> {
            UUID uuid = message.getHeader().getId();
            List<ProcessingInfo> persistedPIs = ProcessingInfo.findProcessingInfo(uuid, "GenerateMessagesFromEvent");
            persistedPIs.forEach(persistedPI -> {
                assertThat(persistedPI.getProcessingId(), CoreMatchers.equalTo(message.getProcessingInfo().getProcessingId()));
                assertThat(persistedPI.getPrevProcessingId(), CoreMatchers.equalTo(message.getProcessingInfo().getPrevProcessingId()));
                assertThat(persistedPI.getPrevProcessingId(), CoreMatchers.equalTo(message.getProcessingInfo().getPrevProcessingId()));
                assertThat(persistedPI.getPrevProcessingId(), CoreMatchers.equalTo(message.getProcessingInfo().getPrevProcessingId()));
                assertThat(persistedPI.getDiffJson(), CoreMatchers.equalTo(message.getProcessingInfo().getDiffJson()));
                assertThat(persistedPI.getStepName(), CoreMatchers.equalTo(message.getProcessingInfo().getStepName()));
                assertThat(persistedPI.getEventJson(), CoreMatchers.equalTo(message.getProcessingInfo().getEventJson()));
                assertThat(persistedPI.getDurationInMs(), CoreMatchers.equalTo(message.getProcessingInfo().getDurationInMs()));
                assertThat(persistedPI.getTimeStampStart(), CoreMatchers.equalTo(message.getProcessingInfo().getTimeStampStart()));
                assertThat(persistedPI.getTimeStampFinish(), CoreMatchers.equalTo(message.getProcessingInfo().getTimeStampFinish()));
            });
        });
    }

    @Test
    void testPersistPIForSendMessage() {
        // given
        String INPUT_JSON_FILE = "events/ba_job_post.json";
        Event event = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Event.class);
        event = validateAndGenerateEventId.apply(event);
        event = resolveRecipients.apply(event);
        List<Message> messages = generateMessagesFromEvent.apply(event);
        messages.forEach(message -> message = functionSend.apply(message));

        // when
        messages.forEach(message -> processingInfoPersister.accept(message));

        // then
        messages.forEach(message -> {
            UUID uuid = message.getHeader().getId();
            List<ProcessingInfo> persistedPIs = ProcessingInfo.findProcessingInfo(uuid, "SendEmail");
            persistedPIs.forEach(persistedPI -> {
                assertThat(persistedPI.getProcessingId(), CoreMatchers.equalTo(message.getProcessingInfo().getProcessingId()));
                assertThat(persistedPI.getPrevProcessingId(), CoreMatchers.equalTo(message.getProcessingInfo().getPrevProcessingId()));
                assertThat(persistedPI.getPrevProcessingId(), CoreMatchers.equalTo(message.getProcessingInfo().getPrevProcessingId()));
                assertThat(persistedPI.getPrevProcessingId(), CoreMatchers.equalTo(message.getProcessingInfo().getPrevProcessingId()));
                assertThat(persistedPI.getDiffJson(), CoreMatchers.equalTo(message.getProcessingInfo().getDiffJson()));
                assertThat(persistedPI.getStepName(), CoreMatchers.equalTo(message.getProcessingInfo().getStepName()));
                assertThat(persistedPI.getEventJson(), CoreMatchers.equalTo(message.getProcessingInfo().getEventJson()));
                assertThat(persistedPI.getDurationInMs(), CoreMatchers.equalTo(message.getProcessingInfo().getDurationInMs()));
                assertThat(persistedPI.getTimeStampStart(), CoreMatchers.equalTo(message.getProcessingInfo().getTimeStampStart()));
                assertThat(persistedPI.getTimeStampFinish(), CoreMatchers.equalTo(message.getProcessingInfo().getTimeStampFinish()));
            });
        });
    }
}
