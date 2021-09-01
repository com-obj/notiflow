package com.obj.nc.controllers;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.MessagePersistantState;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.repositories.*;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@AutoConfigureMockMvc
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest(properties = {
		"nc.jwt.enabled=true",
		"nc.jwt.username=testUser",
		"nc.jwt.password=testPassword",
		"nc.jwt.signature-secret=testSecret"
})
@DirtiesContext
class DeliveryInfoControllerWithAuthenticationTest extends BaseIntegrationTest {
    
    
	@Autowired private DeliveryInfoRepository deliveryRepo;
	@Autowired private MessageRepository messageRepo;
	@Autowired private EndpointsRepository endpointRepo;
	@Autowired protected MockMvc mockMvc;
	@Autowired GenericEventRepository eventRepo;
	

    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
    	purgeNotifTables(jdbcTemplate);
    }
	
	@Test
	void testReadMessageDeliveryInfoUpdate() throws Exception {
		//GIVEN
		EmailEndpoint email1 = EmailEndpoint.builder().email("jancuzy@gmail.com").build();
		email1 = endpointRepo.persistEnpointIfNotExists(email1);
		
    	//AND
		GenericEvent event = GenericEventRepositoryTest.createDirectMessageEvent();
		UUID eventId = eventRepo.save(event).getId();
		
		//AND
		EmailMessage emailMessage = new EmailMessage();
		emailMessage.setRecievingEndpoints(Arrays.asList(email1));
		emailMessage.getHeader().addEventId(eventId);
		emailMessage.getHeader().addMessageId(emailMessage.getId());
		MessagePersistantState emailMessagePersisted = messageRepo.save(emailMessage.toPersistantState());
		
		//AND
		DeliveryInfo info = DeliveryInfo.builder()
				.endpointId(email1.getId())
				.eventId(eventId)
				.status(DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.messageId(emailMessagePersisted.getId())
				.build();		
		deliveryRepo.save(info);
		
		//WHEN TEST REST
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders
						.put(ncAppConfigProperties.getContextPath() + "/delivery-info/messages/{messageId}/mark-as-read", Objects.requireNonNull(emailMessagePersisted.getId()).toString())
						.contextPath(ncAppConfigProperties.getContextPath())
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		//THEN REDIRECT TO IMAGE
		resp
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(ncAppConfigProperties.getContextPath() + "/resources/images/px.png"));
		
		//AND IMAGE IS FOUND
		resp = mockMvc
				.perform(MockMvcRequestBuilders
						.get(ncAppConfigProperties.getContextPath() +  "/resources/images/px.png")
						.contextPath(ncAppConfigProperties.getContextPath()))
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().contentType(MediaType.IMAGE_PNG));
		
		//AND READ STATUS IS JOURNALED
		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> {
			List<DeliveryInfo> infosOfMessage = deliveryRepo.findByMessageIdOrderByProcessedOn(emailMessagePersisted.getId());
			return infosOfMessage.size() >= 2;
		});
		List<DeliveryInfo> infosOfMessage = deliveryRepo.findByMessageIdOrderByProcessedOn(emailMessagePersisted.getId());
		assertThat(infosOfMessage).hasSize(2);
		assertThat(infosOfMessage.get(1).getStatus()).isEqualTo(DELIVERY_STATUS.READ);
	}

}
