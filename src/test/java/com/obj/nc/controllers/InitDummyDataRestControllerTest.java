package com.obj.nc.controllers;

import com.obj.nc.controllers.InitDummyDataRestController.DummyEventPayload;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.repositories.*;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
class InitDummyDataRestControllerTest extends BaseIntegrationTest {
	
	@Autowired private GenericEventRepository genericEventRepository;
	@Autowired private NotificationIntentRepository notificationIntentRepository;
	@Autowired private EndpointsRepository endpointsRepository;
	@Autowired private MessageRepository messageRepository;
	@Autowired private DeliveryInfoRepository deliveryInfoRepository;
	@Autowired private InitDummyDataRestController controller;

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
	}
	
	private void assertGenericEventPersisted() {
		List<GenericEvent> genericEvents = Lists.newArrayList(genericEventRepository.findAll());
		assertThat(genericEvents).hasSize(1);
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
	
}
