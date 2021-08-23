package com.obj.nc.controllers;

import com.obj.nc.controllers.InitDummyDataRestController.DummyEventPayload;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.MessagePersistantState;
import com.obj.nc.domain.message.SmsMessage;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.repositories.*;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.*;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
class InitDummyDataRestControllerTest extends BaseIntegrationTest {
	
	@Autowired private InitDummyDataRestController controller;
	@Autowired private GenericEventRepository genericEventRepository;
	@Autowired private NotificationIntentRepository notificationIntentRepository;
	@Autowired private EndpointsRepository endpointsRepository;
	@Autowired private MessageRepository messageRepository;
	@Autowired private DeliveryInfoRepository deliveryInfoRepository;

    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
    	purgeNotifTables(jdbcTemplate);
    }
    
    @Test
    void testFindDeliveryInfos() {
    	// WHEN
    	controller.initDummyData();
    	// THEN
		assertGenericEventPersisted();
		assertReceivingEndpointsPersisted();
		assertNotificationIntentPersisted();
		assertMessagesPersisted();
		assertDeliveryInfosPersisted();
	}
	
	private void assertGenericEventPersisted() {
		List<GenericEvent> genericEvents = Lists.newArrayList(genericEventRepository.findAll());
		assertThat(genericEvents).hasSize(1);
		assertThat(genericEvents.get(0).getFlowId()).isEqualTo("default-flow");
		DummyEventPayload payload = JsonUtils.readObjectFromJSON(genericEvents.get(0).getPayloadJson(), DummyEventPayload.class);
		assertThat(payload.getIntField()).isEqualTo(15);
		assertThat(payload.getStringField()).isEqualTo("simple string");
	}
	
	private void assertReceivingEndpointsPersisted() {
		List<RecievingEndpoint> receivingEndpoints = Lists.newArrayList(endpointsRepository.findAll());
		assertThat(receivingEndpoints).hasSize(3);
		RecievingEndpoint johnDoeEmail = receivingEndpoints.get(0);
		assertThat(johnDoeEmail.getEndpointId()).isEqualTo("john.doe@objectify.sk");
		RecievingEndpoint johnDudlyEmail = receivingEndpoints.get(1);
		assertThat(johnDudlyEmail.getEndpointId()).isEqualTo("john.dudly@objectify.sk");
		RecievingEndpoint phone = receivingEndpoints.get(2);
		assertThat(phone.getEndpointId()).isEqualTo("0918111111");
	}
	
	private void assertNotificationIntentPersisted() {
		List<NotificationIntent> notificationIntents = Lists.newArrayList(notificationIntentRepository.findAll());
		assertThat(notificationIntents).hasSize(1);
		assertThat(notificationIntents.get(0).getBody().getSubject()).isEqualTo("subject");
		assertThat(notificationIntents.get(0).getBody().getBody()).isEqualTo("body");
		assertThat(notificationIntents.get(0).getEventIds()).hasSize(1);
	}
	
	private void assertMessagesPersisted() {
		List<MessagePersistantState> messages = Lists.newArrayList(messageRepository.findAll());
		assertThat(messages).hasSize(3);
		assertThat(messages.get(0).getMessageClass()).isEqualTo(EmailMessage.class.getName());
		assertThat(messages.get(1).getMessageClass()).isEqualTo(EmailMessage.class.getName());
		assertThat(messages.get(2).getMessageClass()).isEqualTo(SmsMessage.class.getName());
	}
	
	private void assertDeliveryInfosPersisted() {
		List<DeliveryInfo> deliveryInfos = Lists.newArrayList(deliveryInfoRepository.findAll());
		assertThat(deliveryInfos).hasSize(8);
		assertThat(deliveryInfos.get(0).getStatus()).isEqualTo(PROCESSING);
		assertThat(deliveryInfos.get(1).getStatus()).isEqualTo(SENT);
		assertThat(deliveryInfos.get(2).getStatus()).isEqualTo(READ);
		assertThat(deliveryInfos.get(3).getStatus()).isEqualTo(PROCESSING);
		assertThat(deliveryInfos.get(4).getStatus()).isEqualTo(SENT);
		assertThat(deliveryInfos.get(5).getStatus()).isEqualTo(READ);
		assertThat(deliveryInfos.get(6).getStatus()).isEqualTo(PROCESSING);
		assertThat(deliveryInfos.get(7).getStatus()).isEqualTo(FAILED);
	}
	
}
