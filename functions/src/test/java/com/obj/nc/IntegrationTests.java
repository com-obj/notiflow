package com.obj.nc;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.obj.nc.domain.ProcessingInfo;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.EventIdGenerator;
import com.obj.nc.functions.processors.MessagesFromEventBuilder;
import com.obj.nc.functions.processors.koderia.RecepientsUsingKoderiaSubsriptionFinder;
import com.obj.nc.functions.processors.senders.EmailSenderSink;
import com.obj.nc.functions.sink.ProcessingInfoPersister;
import com.obj.nc.utils.GreenMailManager;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;

/*
* TODO: zmenit z @Nested tried s testami na samostatne triedy
*  Dovod, preco su zatial @Nested: neocakavane sa ukoncoval stream z postgresu a restartoval sa container tesne predtym,
*  ako mala zacat dalsia classa testov. Toto bola jedina moznost, ktora zarucuje, ze container bude UP pocas vsetkych
*  SpringBootTestov
 */
@Testcontainers
public class IntegrationTests {
    public static final String DOCKER_COMPOSE_PATH = "../docker-k7s/fn-test-components/docker-compose.yml";

    @Container
    public static DockerComposeContainer<?> environment = new DockerComposeContainer<>(new File(DOCKER_COMPOSE_PATH))
            .withLocalCompose(true);

    @Nested
    @ActiveProfiles("test")
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
    class ProcessingInfoPersisterTest {

        @Autowired
        private ProcessingInfoPersister processingInfoPersister;

        @Autowired
        private EventIdGenerator.ValidateAndGenerateEventId validateAndGenerateEventId;

        @Autowired
        private RecepientsUsingKoderiaSubsriptionFinder.ResolveRecipients resolveRecipients;

        @Autowired
        private MessagesFromEventBuilder.GenerateMessagesFromEvent generateMessagesFromEvent;

        @Autowired
        private EmailSenderSink.SendEmailMessage functionSend;

        @Autowired
        private EmailSenderSink.SendEmailMessageConfig emailFromSetting;

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
            processingInfoPersister.persistPIForEvent().accept(event);

            // then
            Map<String, Object> persistedPI = jdbcTemplate.queryForMap("select * from nc_processing_info where event_id = ?", event.getHeader().getEventId());
            assertThat(persistedPI, CoreMatchers.notNullValue());

            assertThat(persistedPI.get("processing_id"), CoreMatchers.equalTo(event.getProcessingInfo().getProcessingId()));
            assertThat(persistedPI.get("prev_processing_id"), CoreMatchers.equalTo(event.getProcessingInfo().getPrevProcessingId()));

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
            String INPUT_JSON_FILE = "events/direct_message.json";
            Event event = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Event.class);
            event = validateAndGenerateEventId.apply(event);
            event = resolveRecipients.apply(event);

            // when
            processingInfoPersister.persistPIForEventWithRecipients().accept(event);

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
            String INPUT_JSON_FILE = "events/direct_message.json";
            Event event = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Event.class);
            event = validateAndGenerateEventId.apply(event);
            event = resolveRecipients.apply(event);
            List<Message> messages = generateMessagesFromEvent.apply(event);

            // when
            messages.forEach(message -> processingInfoPersister.persistPIForMessage().accept(message));

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
            String INPUT_JSON_FILE = "events/direct_message.json";
            Event event = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Event.class);
            event = validateAndGenerateEventId.apply(event);
            event = resolveRecipients.apply(event);
            List<Message> messages = generateMessagesFromEvent.apply(event);
            messages.forEach(message -> message = functionSend.apply(message));

            // when
            messages.forEach(message -> processingInfoPersister.persistPIForSendMessage().accept(message));

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

    @Nested
    @ActiveProfiles("test")
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
    class EmailSenderSinkTest {

        @Autowired
        private GreenMailManager greenMailManager;

        @Autowired
        private EmailSenderSink.SendEmailMessage functionSend;

        @Autowired
        private EmailSenderSink.SendEmailMessageConfig emailFromSetting;

        @BeforeEach
        void cleanGreenMailMailBoxes() throws FolderException {
            greenMailManager.getGreenMail().purgeEmailFromAllMailboxes();
        }

        @Test
        void sendSingleMail() throws MessagingException, IOException {
            //GIVEN
            String INPUT_JSON_FILE = "messages/email_message.json";
            Message message = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);
            UUID originalProcessingId = message.getProcessingInfo().getProcessingId();

            //WHEN
            Message result = functionSend.apply(message);

            //THEN
            GreenMail gm = greenMailManager.getGreenMail();
            MimeMessage[] messages = gm.getReceivedMessages();
            Assertions.assertThat( messages.length ).isEqualTo(1);
            Assertions.assertThat( messages[0].getSubject() ).isEqualTo("Subject");
            Assertions.assertThat( messages[0].getFrom()[0] ).extracting("address").isEqualTo(emailFromSetting.getFrom());

            //THEN check processing info

            ProcessingInfo processingInfo = result.getProcessingInfo();
            Assertions.assertThat(processingInfo.getStepName()).isEqualTo("SendEmail");
            Assertions.assertThat(processingInfo.getStepIndex()).isEqualTo(4);
            Assertions.assertThat(processingInfo.getPrevProcessingId()).isEqualTo(originalProcessingId);
            Assertions.assertThat(processingInfo.getTimeStampStart()).isBeforeOrEqualTo(processingInfo.getTimeStampFinish());
        }

        @Test
        void sendMailToManyRecipients() {
            //GIVEN
            String INPUT_JSON_FILE = "messages/e_email_message_2_many.json";
            Message message = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);

            //WHEN -THEN
            Assertions.assertThatThrownBy(() -> {
                functionSend.apply(message);
            })
                    .isInstanceOf(PayloadValidationException.class)
                    .hasMessageContaining("Email sender can send to only one recipient. Found more: ");
        }

        @Test
        void sendEmailToNonEmailEnpoint() {
            //GIVEN
            String INPUT_JSON_FILE = "messages/e_email_message_2_push_endpoint.json";
            Message message = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);

            //WHEN -THEN
            Assertions.assertThatThrownBy(() -> {
                functionSend.apply(message);
            })
                    .isInstanceOf(PayloadValidationException.class)
                    .hasMessageContaining("Email sender can send to Email endpoints only. Found ");
        }

        @Test
        void sendMailWithAttachments() {
            //GIVEN
            String INPUT_JSON_FILE = "messages/email_message_attachments.json";
            Message inputMessage = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);

            inputMessage.getBody().getAttachments().forEach(attachement -> {
                try {
                    attachement.setFileURI(new ClassPathResource(attachement.getFileURI().getPath()).getURI());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            //WHEN
            Message outputMessage = functionSend.apply(inputMessage);

            //THEN
            GreenMail gm = greenMailManager.getGreenMail();
            MimeMessage message = gm.getReceivedMessages()[0];
            String body = GreenMailUtil.getBody(message);

            Assertions.assertThat(body).contains("name=test1.txt", "attachment test1", "name=test2.txt", "attachment test2");
        }
    }
}
