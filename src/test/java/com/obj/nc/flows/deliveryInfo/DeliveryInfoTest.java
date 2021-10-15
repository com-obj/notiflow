/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.flows.deliveryInfo;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
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

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.config.SpringIntegration;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.SmsMessage;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlow;
import com.obj.nc.flows.errorHandling.domain.FailedPayload;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.functions.processors.dummy.DummyRecepientsEnrichmentProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromIntentGenerator;
import com.obj.nc.functions.processors.messagePersister.MessagePersister;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.repositories.EndpointsRepository;
import com.obj.nc.repositories.FailedPayloadRepository;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.GenericEventRepositoryTest;
import com.obj.nc.repositories.MessageRepository;
import com.obj.nc.repositories.NotificationIntentRepository;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
public class DeliveryInfoTest extends BaseIntegrationTest {
	
    @Autowired private DummyRecepientsEnrichmentProcessingFunction resolveRecipients;
    @Autowired private MessagesFromIntentGenerator generateMessagesFromIntent;
    @Autowired private MessagePersister messagePersister;
    @Autowired private DeliveryInfoRepository deliveryInfoRepo;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private EmailProcessingFlow emailSendingFlow;
    @Autowired private DeliveryInfoFlow deliveryInfoFlow;
    @Autowired private GenericEventRepository eventRepo;
    @Autowired private NotificationIntentRepository intentRepo;
    @Autowired private EndpointsRepository endpointRepo;
	@Autowired private MessageRepository messageRepo;
	@Autowired private FailedPayloadRepository failedPayloadRepo;
	@Autowired @Qualifier(SpringIntegration.OBJECT_MAPPER_FOR_SPRING_MESSAGES_BEAN_NAME) ObjectMapper jsonConverterForSpringMessages;    
 	
    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
    	purgeNotifTables(jdbcTemplate);
    }

    @Test
    void testDeliveryInfosCreateAndPersisted() {
        // GIVEN
		GenericEvent event = GenericEventRepositoryTest.createDirectMessageEvent();
		UUID eventId = eventRepo.save(event).getId();
		
        String INPUT_JSON_FILE = "intents/ba_job_post.json";
        NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);
        notificationIntent.addPreviousEventId(eventId);
        
        notificationIntent = (NotificationIntent)resolveRecipients.apply(notificationIntent);
        notificationIntent.ensureEndpointsPersisted();
        intentRepo.save(notificationIntent);
        
        //WHEN
        deliveryInfoFlow.createAndPersistProcessingDeliveryInfo(notificationIntent);
        
        //THEN check processing deliveryInfo
        Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> deliveryInfoRepo.findByEventIdOrderByProcessedOn(eventId).size()>=3);
        
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
        	msg = (EmailMessage) messagePersister.apply(msg);
        	
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
    	final EmailEndpoint wrongEmail = endpointRepo.persistEnpointIfNotExists(
    			EmailEndpoint.builder().email("wrong email").build()
    	);
        
		GenericEvent event = GenericEventRepositoryTest.createDirectMessageEvent();
		UUID eventId = eventRepo.save(event).getId();
		
    	EmailMessage email = new EmailMessage();
        email.addReceivingEndpoints(wrongEmail);
        email.addPreviousEventId(eventId);
        
        //WHEN
        emailSendingFlow.sendEmail(email);

        //THEN check processing deliveryInfo
        Awaitility.await().atMost(Duration.ofSeconds(5)).until(() -> deliveryInfoRepo.findByEventIdOrderByProcessedOn(eventId).size()>=1);
        
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
		GenericEvent event = GenericEventRepositoryTest.createDirectMessageEvent();
		UUID eventId = eventRepo.save(event).getId();
		
		SmsEndpoint smsEndpoint = endpointRepo.persistEnpointIfNotExists(new SmsEndpoint("09050123456"));  
		
		//AND GIVEN
    	SmsMessage failedMessage = createTestSMS(eventId, smsEndpoint);
    	messageRepo.save(failedMessage.toPersistentState());
    	org.springframework.messaging.Message<SmsMessage> failedSpringMessage = MessageBuilder.withPayload(failedMessage).build();
    	
    	JsonNode messageJson = jsonConverterForSpringMessages.valueToTree(failedSpringMessage);
    	
    	FailedPayload failedPaylod = FailedPayload.builder()
        		.errorMessage("Error")
        		.exceptionName("Exception")
        		.flowId("flow_id")
        		.id(UUID.randomUUID())
        		.messageJson(messageJson)
        		.build();
    	failedPayloadRepo.save(failedPaylod);
        
        //WHEN
        List<DeliveryInfo> delInfo = deliveryInfoFlow.createAndPersistFailedDeliveryInfo(failedPaylod);


        //THEN check infos
        Assertions.assertThat(delInfo.size()).isEqualTo(2); // 1 for event, 1 for message
        delInfo.forEach(info -> {
        	Assertions.assertThat(info.getStatus()).isEqualTo(DELIVERY_STATUS.FAILED);
        	Assertions.assertThat(info.getProcessedOn()).isNotNull();
        	Assertions.assertThat(info.getEndpointId()).isIn(failedMessage.getReceivingEndpoints().get(0).getId());
        	Assertions.assertThat(info.getFailedPayloadId()).isNotNull();
        });
        
        //THEN check infos in DB
        checkSingleDelInfoExistsForEvent(eventId);
    }
    
    @Test
    void testDeliveryInfosCreateAndPersistedForProcessingDeliveryViaGateway() throws InterruptedException, ExecutionException, TimeoutException {
		// GIVEN    	
		GenericEvent event = GenericEventRepositoryTest.createDirectMessageEvent();
		UUID eventId = eventRepo.save(event).getId();
		
		//AND GIVEN
		SmsEndpoint smsEndpoint = endpointRepo.persistEnpointIfNotExists(new SmsEndpoint("09050123456"));  
    	SmsMessage msg = createTestSMS(eventId, smsEndpoint);
    	messageRepo.save(msg.toPersistentState());
    	        
        //WHEN
        List<DeliveryInfo> delInfo = deliveryInfoFlow.createAndPersistProcessingDeliveryInfo(msg).get(1, TimeUnit.SECONDS);


        //THEN check infos
        Assertions.assertThat(delInfo.size()).isEqualTo(2); // 1 for event, 1 for message
        delInfo.forEach(info -> {
        	Assertions.assertThat(info.getStatus()).isEqualTo(DELIVERY_STATUS.PROCESSING);
        	Assertions.assertThat(info.getProcessedOn()).isNotNull();
        	Assertions.assertThat(info.getEndpointId()).isIn(msg.getReceivingEndpoints().get(0).getId());
        });
        
        //THEN check infos in DB
        checkSingleDelInfoExistsForEvent(eventId);
    }

	private SmsMessage createTestSMS(UUID eventId, SmsEndpoint telNumber) {
		SmsMessage msg = new SmsMessage();
    	msg.setPreviousEventIds(Arrays.asList(eventId));
    	
    	msg.addReceivingEndpoints(telNumber);
		return msg;
	}

	private void checkSingleDelInfoExistsForEvent(UUID eventId) {
		List<DeliveryInfo> deliveryInfosInDB = deliveryInfoRepo.findByEventIdOrderByProcessedOn(eventId);
        Assertions.assertThat(deliveryInfosInDB.size()).isEqualTo(1);
	}
    
    @Test
    void testDeliveryInfosCreateAndPersistedForSentDeliveryViaGateway() throws InterruptedException, ExecutionException, TimeoutException {
		// GIVEN    	
		GenericEvent event = GenericEventRepositoryTest.createDirectMessageEvent();
		UUID eventId = eventRepo.save(event).getId();
		
		//AND GIVEN
		SmsEndpoint smsEndpoint = endpointRepo.persistEnpointIfNotExists(new SmsEndpoint("09050123456"));  
    	SmsMessage msg = createTestSMS(eventId, smsEndpoint);
    	messageRepo.save(msg.toPersistentState());
    	        
        //WHEN
        List<DeliveryInfo> delInfo = deliveryInfoFlow.createAndPersistSentDeliveryInfo(msg).get(1, TimeUnit.SECONDS);

        //THEN check infos
        Assertions.assertThat(delInfo.size()).isEqualTo(2); // 1 for event, 1 for message
        delInfo.forEach(info -> {
        	Assertions.assertThat(info.getStatus()).isEqualTo(DELIVERY_STATUS.SENT);
        	Assertions.assertThat(info.getProcessedOn()).isNotNull();
        	Assertions.assertThat(info.getEndpointId()).isIn(msg.getReceivingEndpoints().get(0).getId());
        });
        
        //THEN check infos in DB
        checkSingleDelInfoExistsForEvent(eventId);
    }


	private void assertEnpointPersistedNotDuplicated(NotificationIntent notificationIntent) {
		List<Map<String, Object>> persistedEndpoints = jdbcTemplate.queryForList("select * from nc_endpoint");
        assertThat(persistedEndpoints, CoreMatchers.notNullValue());
        Assertions.assertThat(persistedEndpoints.size()).isEqualTo(3);

        for (int i = 0; i < persistedEndpoints.size(); i++) {
            List<EmailEndpoint> receivingEndpoints = (List<EmailEndpoint>)notificationIntent.getReceivingEndpoints();
            assertThat(persistedEndpoints.get(i).get("endpoint_name"), CoreMatchers.equalTo(((EmailEndpoint) receivingEndpoints.get(i)).getEmail()));
            assertThat(persistedEndpoints.get(i).get("endpoint_type"), CoreMatchers.equalTo(receivingEndpoints.get(i).getEndpointType()));
        }
	}
	
    private List<UUID> extractReceivingEndpointIds(NotificationIntent finalNotificationIntent) {
        return finalNotificationIntent.getReceivingEndpoints().stream().map(ReceivingEndpoint::getId).collect(Collectors.toList());
    }

}
