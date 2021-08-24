package com.obj.nc.controllers;

import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.headers.ProcessingInfo;
import com.obj.nc.domain.message.MessagePersistantState;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.flows.errorHandling.domain.FailedPaylod;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.repositories.*;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@AutoConfigureMockMvc
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
class InitDummyDataRestControllerTest extends BaseIntegrationTest {
	
	@Autowired private MockMvc mockMvc;
	@Autowired private GenericEventRepository genericEventRepository;
	@Autowired private NotificationIntentRepository notificationIntentRepository;
	@Autowired private EndpointsRepository endpointsRepository;
	@Autowired private MessageRepository messageRepository;
	@Autowired private DeliveryInfoRepository deliveryInfoRepository;
	@Autowired private FailedPayloadRepository failedPayloadRepository;
	@Autowired private ProcessingInfoRepository processingInfoRepository;

    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
    	purgeNotifTables(jdbcTemplate);
    }
    
    @Test
    void testFindDeliveryInfos() throws Exception {
    	// WHEN
		mockMvc
				.perform(MockMvcRequestBuilders.get("/init-dummy-data"))
				.andExpect(status().is2xxSuccessful());
		
		await()
				.atMost(5, TimeUnit.SECONDS)
				.until(() -> processingInfoRepository.count() >= 10);
		
		assertGenericEventPersisted();
		assertReceivingEndpointsPersisted();
		assertNotificationIntentPersisted();
		assertMessagesPersisted();
		assertDeliveryInfosPersisted();
		assertFailedPayloadPersisted();
		assertProcessingInfosPersisted();
	}
	
	private void assertGenericEventPersisted() {
		List<GenericEvent> genericEvents = Lists.newArrayList(genericEventRepository.findAll());
		assertThat(genericEvents).hasSize(1);
	}
	
	private void assertReceivingEndpointsPersisted() {
		List<RecievingEndpoint> receivingEndpoints = Lists.newArrayList(endpointsRepository.findAll());
		assertThat(receivingEndpoints).hasSize(3);
	}
	
	private void assertNotificationIntentPersisted() {
		List<NotificationIntent> notificationIntents = Lists.newArrayList(notificationIntentRepository.findAll());
		assertThat(notificationIntents).hasSize(1);
	}
	
	private void assertMessagesPersisted() {
		List<MessagePersistantState> messages = Lists.newArrayList(messageRepository.findAll());
		assertThat(messages).hasSize(3);
	}
	
	private void assertDeliveryInfosPersisted() {
		List<DeliveryInfo> deliveryInfos = Lists.newArrayList(deliveryInfoRepository.findAll());
		assertThat(deliveryInfos).hasSize(6);
	}
	
	private void assertFailedPayloadPersisted() {
		List<FailedPaylod> failedPayloads = Lists.newArrayList(failedPayloadRepository.findAll());
		assertThat(failedPayloads).hasSize(1);
	}
	
	private void assertProcessingInfosPersisted() {
		List<ProcessingInfo> processingInfos = Lists.newArrayList(processingInfoRepository.findAll());
		assertThat(processingInfos).hasSize(10);
	}
	
}
