package com.obj.nc.functions.sink;

import com.obj.nc.LocalTestContainers;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.koderia.RecepientsUsingKoderiaSubscriptionProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromEventProcessingFunction;
import com.obj.nc.functions.processors.senders.EmailSenderSinkExecution;
import com.obj.nc.functions.processors.senders.EmailSenderSinkProcessingFunction;
import com.obj.nc.functions.sink.processingInfoPersister.ProcessingInfoPersisterSinkConsumer;
import com.obj.nc.functions.sink.processingInfoPersister.eventWithRecipients.ProcessingInfoPersisterForEventWithRecipientsSinkConsumer;
import com.obj.nc.utils.JsonUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ProcessingInfoPersisterTest extends LocalTestContainers {

    @Autowired
    private ProcessingInfoPersisterSinkConsumer processingInfoPersister;

    @Autowired
    private ProcessingInfoPersisterForEventWithRecipientsSinkConsumer processingInfoPersisterForEventWithRecipients;

    @Autowired
    private ValidateAndGenerateEventIdProcessingFunction validateAndGenerateEventId;

    @Autowired
    private RecepientsUsingKoderiaSubscriptionProcessingFunction resolveRecipients;

    @Autowired
    private MessagesFromEventProcessingFunction generateMessagesFromEvent;

    @Autowired
    private EmailSenderSinkProcessingFunction functionSend;

    @Autowired
    private EmailSenderSinkExecution.SendEmailMessageConfig emailFromSetting;

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
        Map<String, Object> persistedPI = jdbcTemplate.queryForMap("select * from nc_processing_info where payload_id = ?", event.getHeader().getId());
        assertThat(persistedPI, CoreMatchers.notNullValue());

        assertThat(persistedPI.get("processing_id"), CoreMatchers.equalTo(event.getProcessingInfo().getProcessingId()));
        assertThat(persistedPI.get("prev_processing_id"), CoreMatchers.equalTo(event.getProcessingInfo().getPrevProcessingId()));

        assertThat(((PGobject) persistedPI.get("event_ids")).getValue(), CoreMatchers.equalTo(event.getHeader().eventIdsAsJSONString()));

        assertThat(persistedPI.get("payload_id"), CoreMatchers.equalTo(event.getHeader().getId()));
        assertThat(persistedPI.get("payload_type"), CoreMatchers.equalTo(event.getPayloadTypeName()));

        assertThat(persistedPI.get("step_name"), CoreMatchers.equalTo(event.getProcessingInfo().getStepName()));
        assertThat(persistedPI.get("step_index"), CoreMatchers.equalTo(event.getProcessingInfo().getStepIndex()));

        long start = event.getProcessingInfo().getTimeStampStart().toEpochMilli();
        long finish = event.getProcessingInfo().getTimeStampFinish().toEpochMilli();
        long duration = finish - start;

        assertThat(((Timestamp) persistedPI.get("time_processing_start")).toInstant().toEpochMilli(), CoreMatchers.equalTo(start));
        assertThat(((Timestamp) persistedPI.get("time_processing_end")).toInstant().toEpochMilli(), CoreMatchers.equalTo(finish));
        assertThat(persistedPI.get("step_duration_ms"), CoreMatchers.equalTo(duration));

        assertThat(((PGobject) persistedPI.get("event_json")).getValue(), CoreMatchers.equalTo(event.toJSONString()));
        assertThat(persistedPI.get("event_json_diff"), CoreMatchers.nullValue());
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
        List<Map<String, Object>> persistedPI = jdbcTemplate.queryForList("select * from nc_processing_info");
        assertThat(persistedPI, CoreMatchers.notNullValue());
        assertThat(persistedPI.size(), CoreMatchers.equalTo(messages.size()));

        for (int i = 0; i < persistedPI.size(); i++) {
            assertThat(persistedPI.get(i).get("processing_id"), CoreMatchers.equalTo(messages.get(i).getProcessingInfo().getProcessingId()));
            assertThat(persistedPI.get(i).get("prev_processing_id"), CoreMatchers.equalTo(messages.get(i).getProcessingInfo().getPrevProcessingId()));

            assertThat(persistedPI.get(i).get("payload_id"), CoreMatchers.equalTo(messages.get(i).getHeader().getId()));
            assertThat(persistedPI.get(i).get("payload_type"), CoreMatchers.equalTo(messages.get(i).getPayloadTypeName()));

            assertThat(persistedPI.get(i).get("step_name"), CoreMatchers.equalTo(messages.get(i).getProcessingInfo().getStepName()));
            assertThat(persistedPI.get(i).get("step_index"), CoreMatchers.equalTo(messages.get(i).getProcessingInfo().getStepIndex()));

            long start = messages.get(i).getProcessingInfo().getTimeStampStart().toEpochMilli();
            long finish = messages.get(i).getProcessingInfo().getTimeStampFinish().toEpochMilli();
            long duration = finish - start;

            assertThat(((Timestamp) persistedPI.get(i).get("time_processing_start")).toInstant().toEpochMilli(), CoreMatchers.equalTo(start));
            assertThat(((Timestamp) persistedPI.get(i).get("time_processing_end")).toInstant().toEpochMilli(), CoreMatchers.equalTo(finish));
            assertThat(persistedPI.get(i).get("step_duration_ms"), CoreMatchers.equalTo(duration));

            assertThat(((PGobject) persistedPI.get(i).get("event_json")).getValue(), CoreMatchers.equalTo(messages.get(i).toJSONString()));
            assertThat(persistedPI.get(i).get("event_json_diff"), CoreMatchers.nullValue());
        }
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
        List<Map<String, Object>> persistedPI = jdbcTemplate.queryForList("select * from nc_processing_info");
        assertThat(persistedPI, CoreMatchers.notNullValue());
        assertThat(persistedPI.size(), CoreMatchers.equalTo(messages.size()));

        for (int i = 0; i < persistedPI.size(); i++) {
            assertThat(persistedPI.get(i).get("processing_id"), CoreMatchers.equalTo(messages.get(i).getProcessingInfo().getProcessingId()));
            assertThat(persistedPI.get(i).get("prev_processing_id"), CoreMatchers.equalTo(messages.get(i).getProcessingInfo().getPrevProcessingId()));

            assertThat(persistedPI.get(i).get("payload_id"), CoreMatchers.equalTo(messages.get(i).getHeader().getId()));
            assertThat(persistedPI.get(i).get("payload_type"), CoreMatchers.equalTo(messages.get(i).getPayloadTypeName()));

            assertThat(persistedPI.get(i).get("step_name"), CoreMatchers.equalTo(messages.get(i).getProcessingInfo().getStepName()));
            assertThat(persistedPI.get(i).get("step_index"), CoreMatchers.equalTo(messages.get(i).getProcessingInfo().getStepIndex()));

            long start = messages.get(i).getProcessingInfo().getTimeStampStart().toEpochMilli();
            long finish = messages.get(i).getProcessingInfo().getTimeStampFinish().toEpochMilli();
            long duration = finish - start;

            assertThat(((Timestamp) persistedPI.get(i).get("time_processing_start")).toInstant().toEpochMilli(), CoreMatchers.equalTo(start));
            assertThat(((Timestamp) persistedPI.get(i).get("time_processing_end")).toInstant().toEpochMilli(), CoreMatchers.equalTo(finish));
            assertThat(persistedPI.get(i).get("step_duration_ms"), CoreMatchers.equalTo(duration));

            assertThat(((PGobject) persistedPI.get(i).get("event_json")).getValue(), CoreMatchers.equalTo(messages.get(i).toJSONString()));
            assertThat(persistedPI.get(i).get("event_json_diff"), CoreMatchers.nullValue());
        }
    }
}
