package com.obj.nc.aspects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.time.Duration;
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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.JsonNode;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.IsTypedJson;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.headers.HasHeader;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.headers.ProcessingInfo;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.processors.dummy.DummyRecepientsEnrichmentProcessingFunction;
import com.obj.nc.functions.processors.eventIdGenerator.GenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromNotificationIntentProcessingFunction;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.functions.sources.genericEvents.GenericEventsSupplier;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.ProcessingInfoRepository;
import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
public class ProcessingInfoGeneratorTest {
	
	@Autowired private GenericEventsSupplier generateEventSupplier;
	@Autowired GenericEventRepository eventRepository;
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
        
    	JsonUtils.resetObjectMapper();
    	JsonUtils.getObjectMapper().addMixIn(IsTypedJson.class, TestPayload.class);
    }
    
	@Test
	void testOneToNProcessor() {
		//GIVEN
		String INPUT_JSON_FILE = "events/direct_message.json";
		NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);

		notificationIntent = (NotificationIntent)generateEventId.apply(notificationIntent);
		//WHEN
		List<Message> result = generateMessagesFromIntent.apply(notificationIntent);
		
		//THEN
		assertThat(result.size()).isEqualTo(3);
		
		Message message = result.get(0);
		Header header = message.getHeader();
		assertThat(header.getFlowId()).isEqualTo(notificationIntent.getHeader().getFlowId());
		assertThat(header.getAttributes())
			.contains(
					entry("custom-proerty1", Arrays.asList("xx","yy")), 
					entry("custom-proerty2", "zz")
			);
	}


    @Test
    void testPersistPIForNewIntent() {
        // given
        NotificationIntent notificationIntent = NotificationIntent.createWithSimpleMessage("test-config", "Hi there!!");
        String notificationIntentJson = notificationIntent.toJSONString();
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
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonTypeInfo(include = As.PROPERTY, use = Id.NAME)
    @JsonSubTypes({ 
    	@Type(value = TestPayload.class, name = "TYPE_NAME")})
    public static class TestPayload implements IsTypedJson {
    	
    	private String content;
    }

    @Test
    void testPersistPIForGenericEvent() {
    	//GIVEN
    	JsonNode payload = JsonUtils.writeObjectToJSONNode(TestPayload.builder().content("Test").build());
    	
		GenericEvent event = GenericEvent.builder()
				.externalId(UUID.randomUUID().toString())
				.flowId("FLOW_ID")
				.id(UUID.randomUUID())
				.payloadJson(payload)
				.build();
		
		eventRepository.save(event);
		
		//WHEN
		GenericEvent eventFromDB = generateEventSupplier.get();

        // when
        // ProcessingInfo persistence is done using aspect and in an async way

        // then
        Assertions.assertThat(
        		eventFromDB.getHeader().getEventIds())
        	.isEqualTo(Arrays.asList(
        			eventFromDB.getHeader().getProcessingInfo().getEventIds())
        );
        UUID eventId = eventFromDB.getId();
        Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> procInfoRepo.findByAnyEventIdAndStepName(eventId, "InputEventSupplier").size()>0);

        List<ProcessingInfo> persistedPIs = procInfoRepo.findByAnyEventIdAndStepName(eventId, "InputEventSupplier");
        Assertions.assertThat(persistedPIs.size()).isEqualTo(1);
        
        ProcessingInfo persistedPI = persistedPIs.iterator().next();

        Assertions.assertThat(persistedPI.getEventIds()[0]).isEqualTo(eventId);
        Assertions.assertThat(persistedPI.getPayloadJsonStart()).isNull();
        Assertions.assertThat(persistedPI.getPayloadJsonEnd()).contains(JsonUtils.writeObjectToJSONString(event.getPayloadJson()));
        Assertions.assertThat(persistedPI.getStepDurationMs()).isGreaterThanOrEqualTo(0);
        Assertions.assertThat(persistedPI.getStepName()).isEqualTo("InputEventSupplier");
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
        
        messages.forEach(message -> {
        	 String messageJsonBeforeSend = message.getBody().toJSONString();
             UUID previosProcessingId = message.getProcessingInfo().getProcessingId();
             
             // when
             // ProcessingInfo persistence is done using aspect and in an async way
             Message sendMessage = functionSend.apply(message);
         	 
             //then
             Assertions.assertThat(sendMessage).isNotNull();
             assertMessagesHaveOriginalEventId(originalEventIDs, Arrays.asList(sendMessage));
             
             UUID eventId = originalEventIDs[0];
             Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> procInfoRepo.findByAnyEventIdAndStepName(eventId, "SendEmail").size()>0);
            
             List<ProcessingInfo> persistedPIs = procInfoRepo.findByAnyEventIdAndStepName(eventId, "SendEmail");
             
             Assertions.assertThat(persistedPIs.size()).isEqualTo(1);
             
             ProcessingInfo persistedPI = persistedPIs.iterator().next();

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
             
             procInfoRepo.delete(persistedPI); //not to interfere with next iteration
        });
    }
    
    
    @RegisterExtension
    protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
      	.withConfiguration(
      			GreenMailConfiguration.aConfig()
      			.withUser("no-reply@objectify.sk", "xxx"))
      	.withPerMethodLifecycle(true);

}
