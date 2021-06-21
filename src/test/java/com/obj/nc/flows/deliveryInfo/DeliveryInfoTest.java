package com.obj.nc.flows.deliveryInfo;

import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.DELIVERY_INFO_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowConfig.EMAIL_SENDING_FLOW_INPUT_CHANNEL_ID;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.processors.dummy.DummyRecepientsEnrichmentProcessingFunction;
import com.obj.nc.functions.processors.eventIdGenerator.GenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromNotificationIntentProcessingFunction;
import com.obj.nc.functions.sink.deliveryInfoPersister.domain.DeliveryInfo;
import com.obj.nc.functions.sink.deliveryInfoPersister.domain.DeliveryInfo.DELIVERY_STATUS;
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
    
    @Autowired
    @Qualifier(DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID)
    private MessageChannel deliveryInfoSendInputChannel;
    
    @Autowired
    @Qualifier(DELIVERY_INFO_PROCESSING_FLOW_INPUT_CHANNEL_ID)
    private MessageChannel deliveryInfoProcessingInputChannel;
    
    @Autowired
    @Qualifier(EMAIL_SENDING_FLOW_INPUT_CHANNEL_ID)
    private MessageChannel emailSendingInputChannel;
 	
    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
    	purgeNotifTables(jdbcTemplate);
    }

    @Test
    void testDeliveryInfosCreateAndPersisted() {
        // GIVEN
    	MessagingTemplate messageTemplate = new MessagingTemplate();
    	
        String INPUT_JSON_FILE = "events/ba_job_post.json";
        NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);

        notificationIntent = (NotificationIntent)generateEventId.apply(notificationIntent);
        UUID eventId = notificationIntent.getHeader().getEventIds().get(0);
        notificationIntent = resolveRecipients.apply(notificationIntent);
        
        //WHEN
        org.springframework.messaging.Message<NotificationIntent> notifIntentMsg = MessageBuilder.withPayload(notificationIntent).build();
        messageTemplate.send(deliveryInfoProcessingInputChannel, notifIntentMsg);

        //THEN check processing deliveryInfo
        Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> deliveryInfoRepo.findByEventIdOrderByProcessedOn(eventId).size()==3);
        
        assertEnpointPersistedNotDuplicated(notificationIntent);
        List<DeliveryInfo> deliveryInfos = deliveryInfoRepo.findByEventIdOrderByProcessedOn(eventId);
        
        Assertions.assertThat(deliveryInfos.size()).isEqualTo(3);
        deliveryInfos.forEach(info -> {
        	Assertions.assertThat(info.getStatus()).isEqualTo(DELIVERY_STATUS.PROCESSING);
        	Assertions.assertThat(info.getProcessedOn()).isNotNull();
        	Assertions.assertThat(info.getEndpointId()).isIn("john.doe@objectify.sk","john.dudly@objectify.sk", "all@objectify.sk");
        });
        
        //WHEN
        List<Message> messages = generateMessagesFromIntent.apply(notificationIntent);
        
        messages.forEach(msg -> {
            org.springframework.messaging.Message<Message> notifMsg = MessageBuilder.withPayload(msg).build();
            messageTemplate.send(deliveryInfoSendInputChannel, notifMsg);
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
        	Assertions.assertThat(info.getEndpointId()).isIn("john.doe@objectify.sk","john.dudly@objectify.sk", "all@objectify.sk");
        });
    }
    
    @Test
    void testDeliveryInfosCreateAndPersistedForFailedDelivery() {
        // GIVEN    	
        Message email = Message.createAsEmail();
        email.getBody().addRecievingEndpoints(
        		EmailEndpoint.builder().email("wrong email").build());
        UUID eventId = UUID.randomUUID();
        email.getHeader().addEventId(eventId);
        
        //WHEN
    	MessagingTemplate messageTemplate = new MessagingTemplate();
        messageTemplate.send(emailSendingInputChannel, MessageBuilder.withPayload(email).build());

        //THEN check processing deliveryInfo
        Awaitility.await().atMost(Duration.ofSeconds(1)).until(() -> deliveryInfoRepo.findByEventIdOrderByProcessedOn(eventId).size()==1);
        
        List<DeliveryInfo> deliveryInfos = deliveryInfoRepo.findByEventIdOrderByProcessedOn(eventId);
        
        Assertions.assertThat(deliveryInfos.size()).isEqualTo(1);
        deliveryInfos.forEach(info -> {
        	Assertions.assertThat(info.getStatus()).isEqualTo(DELIVERY_STATUS.FAILED);
        	Assertions.assertThat(info.getProcessedOn()).isNotNull();
        	Assertions.assertThat(info.getEndpointId()).isIn("wrong email");
        	Assertions.assertThat(info.getFailedPayloadId()).isNotNull();
        });
    }


	private void assertEnpointPersistedNotDuplicated(NotificationIntent notificationIntent) {
		List<Map<String, Object>> persistedEndpoints = jdbcTemplate.queryForList("select * from nc_endpoint");
        assertThat(persistedEndpoints, CoreMatchers.notNullValue());
        Assertions.assertThat(persistedEndpoints.size()).isEqualTo(3);

        for (int i = 0; i < persistedEndpoints.size(); i++) {
            List<RecievingEndpoint> recievingEndpoints = notificationIntent.getBody().getRecievingEndpoints();
            assertThat(persistedEndpoints.get(i).get("endpoint_id"), CoreMatchers.equalTo(((EmailEndpoint) recievingEndpoints.get(i)).getEmail()));
            assertThat(persistedEndpoints.get(i).get("endpoint_type"), CoreMatchers.equalTo(recievingEndpoints.get(i).getEndpointType()));
        }
	}

}
