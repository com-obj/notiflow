package com.obj.nc.functions.sink;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.headers.HasHeader;
import com.obj.nc.domain.headers.ProcessingInfo;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.processors.dummy.DummyRecepientsEnrichmentProcessingFunction;
import com.obj.nc.functions.processors.eventIdGenerator.GenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromNotificationIntentProcessingFunction;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.repositories.ProcessingInfoRepository;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
class ProcessingInfoGeneratorTest extends BaseIntegrationTest {

	@Autowired private GenerateEventIdProcessingFunction generateEventId;
    @Autowired private DummyRecepientsEnrichmentProcessingFunction resolveRecipients;
    @Autowired private MessagesFromNotificationIntentProcessingFunction generateMessagesFromIntent;
    @Autowired private EmailSender functionSend;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private ProcessingInfoRepository procInfoRepo;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("truncate table nc_processing_info");
        jdbcTemplate.execute("truncate table nc_endpoint");
    }
    
    @RegisterExtension
    protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
      	.withConfiguration(
      			GreenMailConfiguration.aConfig()
      			.withUser("no-reply@objectify.sk", "xxx"))
      	.withPerMethodLifecycle(true);


    @Test
    void testPersistPIForNewIntent() {
        // given
    	String notificationIntentJson = NotificationIntent.createWithSimpleMessage("test-config", "Hi there!!").toJSONString();
        NotificationIntent notificationIntent = NotificationIntent.createWithSimpleMessage("test-config", "Hi there!!");
        HasHeader payloadWithEventId = generateEventId.apply(notificationIntent);

        // when
        // ProcessingInfo persistence is done using aspect and in an async way

        // then
        Assertions.assertThat(
        		payloadWithEventId.getHeader().getEventIds())
        	.isEqualTo(Arrays.asList(
        			payloadWithEventId.getHeader().getProcessingInfo().getEventIds())
        );
        UUID eventId = payloadWithEventId.getHeader().getProcessingInfo().getEventIds()[0];
        Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> procInfoRepo.findByAnyEventIdAndStepName(eventId, "GenerateEventId").size()>0);

        List<ProcessingInfo> persistedPIs = procInfoRepo.findByAnyEventIdAndStepName(eventId, "GenerateEventId");
        Assertions.assertThat(persistedPIs.size()).isEqualTo(1);
        
        ProcessingInfo persistedPI = persistedPIs.iterator().next();
        
        ProcessingInfo calculatedPI = payloadWithEventId.getHeader().getProcessingInfo();
        Assertions.assertThat(persistedPI.getEventIds()[0]).isEqualTo(calculatedPI.getEventIds()[0]);
        Assertions.assertThat(persistedPI.getPayloadJsonStart()).isEqualTo(notificationIntentJson);
        Assertions.assertThat(persistedPI.getPayloadJsonEnd().length()).isGreaterThan(notificationIntent.toJSONString().length());
        Assertions.assertThat(persistedPI.getStepDurationMs()).isGreaterThanOrEqualTo(0);
        Assertions.assertThat(persistedPI.getStepName()).isEqualTo("GenerateEventId");
        Assertions.assertThat(persistedPI.getTimeProcessingStart()).isNotNull();
        Assertions.assertThat(persistedPI.getTimeProcessingEnd()).isNotNull();
        Assertions.assertThat(persistedPI.getProcessingId()).isNotNull();
        Assertions.assertThat(persistedPI.getPrevProcessingId()).isNull();
        Assertions.assertThat(persistedPI.getStepIndex()).isEqualTo(0);
    }

    @Test
    void testPersistPIForMessageFromIntentStep() {
        // given
        String INPUT_JSON_FILE = "events/ba_job_post.json";
        NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
        notificationIntent = (NotificationIntent)generateEventId.apply(notificationIntent);
        UUID[] originalEventIDs = notificationIntent.getProcessingInfo().getEventIds();
        notificationIntent = resolveRecipients.apply(notificationIntent);
        List<Message> messages = generateMessagesFromIntent.apply(notificationIntent);

        // ProcessingInfo persistence is done using aspect and in an async way

        // then
        Assertions.assertThat(messages.size()).isEqualTo(3);
        assertMessagesHaveOriginalEventId(originalEventIDs, messages);
            
        UUID eventId = originalEventIDs[0];
        Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> procInfoRepo.findByAnyEventIdAndStepName(eventId, "GenerateMessagesFromIntent").size()>0);
       
        List<ProcessingInfo> persistedPIs = procInfoRepo.findByAnyEventIdAndStepName(eventId, "GenerateMessagesFromIntent");
        
        Assertions.assertThat(persistedPIs.size()).isEqualTo(3);
        
        ProcessingInfo persistedPI = persistedPIs.iterator().next();
        Message message = messages.iterator().next();

        Assertions.assertThat(persistedPI.getEventIds()[0]).isEqualTo(eventId);
        Assertions.assertThat(persistedPI.getPayloadJsonStart()).contains(notificationIntent.getBody().toJSONString());
        Assertions.assertThat(persistedPI.getPayloadJsonEnd()).contains(message.getBody().getMessage().toJSONString());
        Assertions.assertThat(persistedPI.getStepDurationMs()).isGreaterThanOrEqualTo(0);
        Assertions.assertThat(persistedPI.getStepName()).isEqualTo("GenerateMessagesFromIntent");
        Assertions.assertThat(persistedPI.getTimeProcessingStart()).isNotNull();
        Assertions.assertThat(persistedPI.getTimeProcessingEnd()).isAfterOrEqualTo(persistedPI.getTimeProcessingStart());
        Assertions.assertThat(persistedPI.getProcessingId()).isNotNull();
        Assertions.assertThat(persistedPI.getPrevProcessingId()).isEqualTo(notificationIntent.getProcessingInfo().getProcessingId());
        Assertions.assertThat(persistedPI.getStepIndex()).isEqualTo(2);

    }

	private void assertMessagesHaveOriginalEventId(UUID[] originalEventIDs, List<Message> messages) {
		messages.forEach(message -> {
        	
            Assertions.assertThat(
            		message.getProcessingInfo().getEventIds())
            	.isEqualTo(originalEventIDs);
        	
            Assertions.assertThat(
            		message.getHeader().getEventIds())
            	.isEqualTo(Arrays.asList(
            		message.getHeader().getProcessingInfo().getEventIds())
            );
        });
	}

    @Test
    void testPersistPIForSendMessage() {
        // given
        String INPUT_JSON_FILE = "events/ba_job_post.json";
        NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
        notificationIntent = (NotificationIntent)generateEventId.apply(notificationIntent);
        UUID[] originalEventIDs = notificationIntent.getProcessingInfo().getEventIds();
        notificationIntent = resolveRecipients.apply(notificationIntent);
        List<Message> messages = generateMessagesFromIntent.apply(notificationIntent);
        String messageJsonBeforeSend = messages.iterator().next().getBody().toJSONString();
        UUID previosProcessingId = messages.iterator().next().getProcessingInfo().getProcessingId();
        
        List<Message> sendMessages = new ArrayList<>();
        messages.forEach(message -> {
        	Message sendMessage = functionSend.apply(message);
        	sendMessages.add(sendMessage);
        });

        // when
        // ProcessingInfo persistence is done using aspect and in an async way

        // then
        Assertions.assertThat(sendMessages.size()).isEqualTo(3);
        assertMessagesHaveOriginalEventId(originalEventIDs, sendMessages);
        
        UUID eventId = originalEventIDs[0];
        Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> procInfoRepo.findByAnyEventIdAndStepName(eventId, "SendEmail").size()>0);
       
        List<ProcessingInfo> persistedPIs = procInfoRepo.findByAnyEventIdAndStepName(eventId, "SendEmail");
        
        Assertions.assertThat(persistedPIs.size()).isEqualTo(3);
        
        ProcessingInfo persistedPI = persistedPIs.iterator().next();
        Message sendMessage = sendMessages.iterator().next();

        Assertions.assertThat(persistedPI.getEventIds()[0]).isEqualTo(eventId);
        Assertions.assertThat(persistedPI.getPayloadJsonStart()).contains(messageJsonBeforeSend);
        Assertions.assertThat(persistedPI.getPayloadJsonEnd()).contains(sendMessage.getBody().getMessage().toJSONString());
        Assertions.assertThat(persistedPI.getStepDurationMs()).isGreaterThanOrEqualTo(0);
        Assertions.assertThat(persistedPI.getStepName()).isEqualTo("SendEmail");
        Assertions.assertThat(persistedPI.getTimeProcessingStart()).isNotNull();
        Assertions.assertThat(persistedPI.getTimeProcessingEnd()).isAfterOrEqualTo(persistedPI.getTimeProcessingStart());
        Assertions.assertThat(persistedPI.getProcessingId()).isNotNull();
        Assertions.assertThat(persistedPI.getPrevProcessingId()).isEqualTo(previosProcessingId);
        Assertions.assertThat(persistedPI.getStepIndex()).isEqualTo(3);
    }
}
