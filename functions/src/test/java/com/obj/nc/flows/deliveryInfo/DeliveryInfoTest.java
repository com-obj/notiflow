package com.obj.nc.flows.deliveryInfo;

import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.obj.nc.domain.endpoints.RecievingEndpoint;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.config.SpringIntegration;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.SmstMessage;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlow;
import com.obj.nc.flows.errorHandling.domain.FailedPaylod;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.functions.processors.dummy.DummyRecepientsEnrichmentProcessingFunction;
import com.obj.nc.functions.processors.eventIdGenerator.GenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromNotificationIntentProcessingFunction;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
public class DeliveryInfoTest extends BaseIntegrationTest {
	
	@Autowired private GenerateEventIdProcessingFunction generateEventId;
    @Autowired private DummyRecepientsEnrichmentProcessingFunction resolveRecipients;
    @Autowired private MessagesFromNotificationIntentProcessingFunction generateMessagesFromIntent;
    @Autowired private DeliveryInfoRepository deliveryInfoRepo;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private EmailProcessingFlow emailSendingFlow;
    @Autowired private DeliveryInfoFlow deliveryInfoFlow;
	@Autowired @Qualifier(SpringIntegration.OBJECT_MAPPER_FOR_SPRING_MESSAGES_BEAN_NAME) ObjectMapper jsonConverterForSpringMessages;    
 	
    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
    	purgeNotifTables(jdbcTemplate);
    }

    @Test
    void testDeliveryInfosCreateAndPersisted() {
        // GIVEN
        String INPUT_JSON_FILE = "intents/ba_job_post.json";
        NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);

        notificationIntent = (NotificationIntent)generateEventId.apply(notificationIntent);
        UUID eventId = notificationIntent.getHeader().getEventIds().get(0);
        notificationIntent = (NotificationIntent)resolveRecipients.apply(notificationIntent);
        
        //WHEN
        deliveryInfoFlow.createAndPersistProcessingDeliveryInfo(notificationIntent);
        
        //THEN check processing deliveryInfo
        Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> deliveryInfoRepo.findByEventIdOrderByProcessedOn(eventId).size()==3);
        
        assertEnpointPersistedNotDuplicated(notificationIntent);
        List<DeliveryInfo> deliveryInfos = deliveryInfoRepo.findByEventIdOrderByProcessedOn(eventId);
        
        Assertions.assertThat(deliveryInfos.size()).isEqualTo(3);
        
        final NotificationIntent finalNotificationIntent = notificationIntent;
        deliveryInfos.forEach(info -> {
        	Assertions.assertThat(info.getStatus()).isEqualTo(DELIVERY_STATUS.PROCESSING);
        	Assertions.assertThat(info.getProcessedOn()).isNotNull();
        	Assertions.assertThat(info.getEndpointId()).isIn(extractReceivingEndpointIds(finalNotificationIntent));
        });
        
        //WHEN
        List<EmailMessage> messages = (List<EmailMessage>)generateMessagesFromIntent.apply(notificationIntent);
        
        messages.forEach(msg -> {
            deliveryInfoFlow.createAndPersistSentDeliveryInfo(msg);
        });
        
        //THEN check delivered deliveryInfo
        Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> deliveryInfoRepo.findByEventIdOrderByProcessedOn(eventId).size()==6);

        deliveryInfos = deliveryInfoRepo.findByEventIdOrderByProcessedOn(eventId);
        assertEnpointPersistedNotDuplicated(notificationIntent);
        
        
        Assertions.assertThat(deliveryInfos.size()).isEqualTo(6);
        List<DeliveryInfo> deliveredInfos = deliveryInfos.stream().filter(info -> info.getStatus() == DELIVERY_STATUS.SENT).collect(Collectors.toList());
        
        Assertions.assertThat(deliveredInfos.size()).isEqualTo(3);
        deliveryInfos.forEach(info -> {
        	Assertions.assertThat(info.getProcessedOn()).isNotNull();
        	Assertions.assertThat(info.getEndpointId()).isIn(extractReceivingEndpointIds(finalNotificationIntent));
        });
    }
    
    @Test
    void testDeliveryInfosCreateAndPersistedForFailedDelivery() throws InterruptedException, ExecutionException, TimeoutException {
        // GIVEN    	
    	EmailMessage email = new EmailMessage();
        EmailEndpoint wrongEmail = EmailEndpoint.builder().email("wrong email").build();
        email.addRecievingEndpoints(
                wrongEmail);
        UUID eventId = UUID.randomUUID();
        email.getHeader().addEventId(eventId);
        
        //WHEN
        emailSendingFlow.sendEmail(email);

        //THEN check processing deliveryInfo
        Awaitility.await().atMost(Duration.ofSeconds(1)).until(() -> deliveryInfoRepo.findByEventIdOrderByProcessedOn(eventId).size()==1);
        
        List<DeliveryInfo> deliveryInfos = deliveryInfoRepo.findByEventIdOrderByProcessedOn(eventId);
        
        Assertions.assertThat(deliveryInfos.size()).isEqualTo(1);
        deliveryInfos.forEach(info -> {
        	Assertions.assertThat(info.getStatus()).isEqualTo(DELIVERY_STATUS.FAILED);
        	Assertions.assertThat(info.getProcessedOn()).isNotNull();
        	Assertions.assertThat(info.getEndpointId()).isIn(wrongEmail.getId());
        	Assertions.assertThat(info.getFailedPayloadId()).isNotNull();
        });
    }
    
    @Test
    void testDeliveryInfosCreateAndPersistedForFailedDeliveryViaGateway() throws InterruptedException, ExecutionException, TimeoutException {
		// GIVEN    	
    	UUID eventId = UUID.randomUUID();
    	SmstMessage failedMessage = createTestSMS(eventId, "09050123456");
    	org.springframework.messaging.Message<SmstMessage> failedSpringMessage = MessageBuilder.withPayload(failedMessage).build();
    	
    	JsonNode messageJson = jsonConverterForSpringMessages.valueToTree(failedSpringMessage);
    	
    	FailedPaylod failedPaylod = FailedPaylod.builder()
        		.errorMessage("Error")
        		.exceptionName("Exception")
        		.flowId("flow_id")
        		.id(UUID.randomUUID())
        		.messageJson(messageJson)
        		.build();
        
        //WHEN
        List<DeliveryInfo> delInfo = deliveryInfoFlow.createAndPersistFailedDeliveryInfo(failedPaylod).get(1, TimeUnit.SECONDS);


        //THEN check infos
        Assertions.assertThat(delInfo.size()).isEqualTo(1);
        delInfo.forEach(info -> {
        	Assertions.assertThat(info.getStatus()).isEqualTo(DELIVERY_STATUS.FAILED);
        	Assertions.assertThat(info.getProcessedOn()).isNotNull();
        	Assertions.assertThat(info.getEndpointId()).isIn(failedMessage.getRecievingEndpoints().get(0).getId());
        	Assertions.assertThat(info.getFailedPayloadId()).isNotNull();
        });
        
        //THEN check infos in DB
        checkSingleDelInfoExistsForEvent(eventId);
    }
    
    @Test
    void testDeliveryInfosCreateAndPersistedForProcessingDeliveryViaGateway() throws InterruptedException, ExecutionException, TimeoutException {
		// GIVEN    	
    	UUID eventId = UUID.randomUUID();
    	SmstMessage msg = createTestSMS(eventId, "09050123456");
    	        
        //WHEN
        List<DeliveryInfo> delInfo = deliveryInfoFlow.createAndPersistProcessingDeliveryInfo(msg).get(1, TimeUnit.SECONDS);


        //THEN check infos
        Assertions.assertThat(delInfo.size()).isEqualTo(1);
        delInfo.forEach(info -> {
        	Assertions.assertThat(info.getStatus()).isEqualTo(DELIVERY_STATUS.PROCESSING);
        	Assertions.assertThat(info.getProcessedOn()).isNotNull();
        	Assertions.assertThat(info.getEndpointId()).isIn(msg.getRecievingEndpoints().get(0).getId());
        });
        
        //THEN check infos in DB
        checkSingleDelInfoExistsForEvent(eventId);
    }

	private SmstMessage createTestSMS(UUID eventId, String telNumber) {
		SmstMessage msg = new SmstMessage();
    	msg.getHeader().setEventIds(Arrays.asList(eventId));
    	msg.addRecievingEndpoints(new SmsEndpoint(telNumber));
		return msg;
	}

	private void checkSingleDelInfoExistsForEvent(UUID eventId) {
		List<DeliveryInfo> deliveryInfosInDB = deliveryInfoRepo.findByEventIdOrderByProcessedOn(eventId);
        Assertions.assertThat(deliveryInfosInDB.size()).isEqualTo(1);
	}
    
    @Test
    void testDeliveryInfosCreateAndPersistedForSentDeliveryViaGateway() throws InterruptedException, ExecutionException, TimeoutException {
		// GIVEN    	
    	UUID eventId = UUID.randomUUID();
    	SmstMessage msg = createTestSMS(eventId, "09050123456");
    	        
        //WHEN
        List<DeliveryInfo> delInfo = deliveryInfoFlow.createAndPersistSentDeliveryInfo(msg).get(1, TimeUnit.SECONDS);

        //THEN check infos
        Assertions.assertThat(delInfo.size()).isEqualTo(1);
        delInfo.forEach(info -> {
        	Assertions.assertThat(info.getStatus()).isEqualTo(DELIVERY_STATUS.SENT);
        	Assertions.assertThat(info.getProcessedOn()).isNotNull();
        	Assertions.assertThat(info.getEndpointId()).isIn(msg.getRecievingEndpoints().get(0).getId());
        });
        
        //THEN check infos in DB
        checkSingleDelInfoExistsForEvent(eventId);
    }


	private void assertEnpointPersistedNotDuplicated(NotificationIntent notificationIntent) {
		List<Map<String, Object>> persistedEndpoints = jdbcTemplate.queryForList("select * from nc_endpoint");
        assertThat(persistedEndpoints, CoreMatchers.notNullValue());
        Assertions.assertThat(persistedEndpoints.size()).isEqualTo(3);

        for (int i = 0; i < persistedEndpoints.size(); i++) {
            List<EmailEndpoint> recievingEndpoints = (List<EmailEndpoint>)notificationIntent.getRecievingEndpoints();
            assertThat(persistedEndpoints.get(i).get("endpoint_name"), CoreMatchers.equalTo(((EmailEndpoint) recievingEndpoints.get(i)).getEmail()));
            assertThat(persistedEndpoints.get(i).get("endpoint_type"), CoreMatchers.equalTo(recievingEndpoints.get(i).getEndpointType()));
        }
	}
	
    private List<UUID> extractReceivingEndpointIds(NotificationIntent finalNotificationIntent) {
        return finalNotificationIntent.getRecievingEndpoints().stream().map(RecievingEndpoint::getId).collect(Collectors.toList());
    }

}
