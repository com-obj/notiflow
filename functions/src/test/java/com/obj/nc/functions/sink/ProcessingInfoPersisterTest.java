package com.obj.nc.functions.sink;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.ProcessingInfo;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.processors.dummy.DummyRecepientsEnrichmentProcessingFunction;
import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromNotificationIntentProcessingFunction;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.functions.sink.processingInfoPersister.ProcessingInfoPersisterSinkConsumer;
import com.obj.nc.functions.sink.processingInfoPersister.eventWithRecipients.ProcessingInfoPersisterForEventWithRecipientsSinkConsumer;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class ProcessingInfoPersisterTest extends BaseIntegrationTest {

	@Autowired private ProcessingInfoPersisterSinkConsumer processingInfoPersister;
	@Autowired private ProcessingInfoPersisterForEventWithRecipientsSinkConsumer processingInfoPersisterForEventWithRecipients;
	@Autowired private ValidateAndGenerateEventIdProcessingFunction validateAndGenerateEventId;
    @Autowired private DummyRecepientsEnrichmentProcessingFunction resolveRecipients;
    @Autowired private MessagesFromNotificationIntentProcessingFunction generateMessagesFromEvent;
    @Autowired private EmailSender functionSend;
    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("truncate table nc_processing_info");
        jdbcTemplate.execute("truncate table nc_endpoint");
    }

    @Test
    void testPersistPIForEvent() {
        // given
        NotificationIntent notificationIntent = NotificationIntent.createWithSimpleMessage("test-config", "Hi there!!");
        notificationIntent = validateAndGenerateEventId.apply(notificationIntent);

        // when
        processingInfoPersister.accept(notificationIntent);

        // then
        UUID uuid = notificationIntent.getHeader().getId();
        final NotificationIntent finalNotificationIntent = notificationIntent;

        List<ProcessingInfo> persistedPIs = ProcessingInfo.findProcessingInfo(uuid, "GenerateMessagesFromEvent");
        persistedPIs.forEach(persistedPI -> {
            assertThat(persistedPI.getProcessingId(), CoreMatchers.equalTo(finalNotificationIntent.getProcessingInfo().getProcessingId()));
            assertThat(persistedPI.getPrevProcessingId(), CoreMatchers.equalTo(finalNotificationIntent.getProcessingInfo().getPrevProcessingId()));
            assertThat(persistedPI.getPrevProcessingId(), CoreMatchers.equalTo(finalNotificationIntent.getProcessingInfo().getPrevProcessingId()));
            assertThat(persistedPI.getPrevProcessingId(), CoreMatchers.equalTo(finalNotificationIntent.getProcessingInfo().getPrevProcessingId()));
            assertThat(persistedPI.getDiffJson(), CoreMatchers.equalTo(finalNotificationIntent.getProcessingInfo().getDiffJson()));
            assertThat(persistedPI.getStepName(), CoreMatchers.equalTo(finalNotificationIntent.getProcessingInfo().getStepName()));
            assertThat(persistedPI.getEventJson(), CoreMatchers.equalTo(finalNotificationIntent.getProcessingInfo().getEventJson()));
            assertThat(persistedPI.getDurationInMs(), CoreMatchers.equalTo(finalNotificationIntent.getProcessingInfo().getDurationInMs()));
            assertThat(persistedPI.getTimeStampStart(), CoreMatchers.equalTo(finalNotificationIntent.getProcessingInfo().getTimeStampStart()));
            assertThat(persistedPI.getTimeStampFinish(), CoreMatchers.equalTo(finalNotificationIntent.getProcessingInfo().getTimeStampFinish()));
        });
    }

    @Test
    void testPersistPIForEventWithRecipients() {
        // given
        String INPUT_JSON_FILE = "events/ba_job_post.json";
        NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
        notificationIntent = validateAndGenerateEventId.apply(notificationIntent);
        notificationIntent = resolveRecipients.apply(notificationIntent);

        // when
        processingInfoPersisterForEventWithRecipients.accept(notificationIntent);

        // then
        List<Map<String, Object>> persistedEndpoints = jdbcTemplate.queryForList("select * from nc_endpoint");
        assertThat(persistedEndpoints, CoreMatchers.notNullValue());

        for (int i = 0; i < persistedEndpoints.size(); i++) {
            List<RecievingEndpoint> recievingEndpoints = notificationIntent.getBody().getRecievingEndpoints();
            assertThat(persistedEndpoints.get(i).get("endpoint_name"), CoreMatchers.equalTo(((EmailEndpoint) recievingEndpoints.get(i)).getEmail()));
            assertThat(persistedEndpoints.get(i).get("endpoint_type"), CoreMatchers.equalTo(recievingEndpoints.get(i).getEndpointTypeName()));
        }
    }

    @Test
    void testPersistPIForMessage() {
        // given
        String INPUT_JSON_FILE = "events/ba_job_post.json";
        NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
        notificationIntent = validateAndGenerateEventId.apply(notificationIntent);
        notificationIntent = resolveRecipients.apply(notificationIntent);
        List<Message> messages = generateMessagesFromEvent.apply(notificationIntent);

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
        NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
        notificationIntent = validateAndGenerateEventId.apply(notificationIntent);
        notificationIntent = resolveRecipients.apply(notificationIntent);
        List<Message> messages = generateMessagesFromEvent.apply(notificationIntent);
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
