package com.obj.nc.controllers;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.config.NcAppConfigProperties;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.MessagePersistantState;
import com.obj.nc.flows.messageProcessing.MessageProcessingFlow;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.repositories.*;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.READ;
import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.SENT;
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
	@Autowired private MessageProcessingFlow messageProcessingFlow;
	@Autowired private NcAppConfigProperties ncAppConfigProperties;
	
	@RegisterExtension
	protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
			.withConfiguration(
					GreenMailConfiguration.aConfig()
							.withUser("no-reply@objectify.sk", "xxx"))
			.withPerMethodLifecycle(true);
	
	@BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
    	purgeNotifTables(jdbcTemplate);
    }
	
	@Test
	void testMarkAsReadMessageDeliveryInfo() throws Exception {
		//GIVEN
		EmailEndpoint email1 = EmailEndpoint.builder().email("john.doe@gmail.com").build();
		email1 = endpointRepo.persistEnpointIfNotExists(email1);
		EmailEndpoint email2 = EmailEndpoint.builder().email("john.dudly@gmail.com").build();
		email2 = endpointRepo.persistEnpointIfNotExists(email2);
		
		//AND
		EmailMessage emailMessage = new EmailMessage();
		emailMessage.setBody(EmailContent.builder().subject("Subject").text("Text").build());
		emailMessage.setRecievingEndpoints(Arrays.asList(email1, email2));
		
		messageProcessingFlow.processMessage(emailMessage);
		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> deliveryRepo.countByMessageIdAndStatus(emailMessage.getId(), SENT) == 2);
		
		List<MessagePersistantState> messages = StreamSupport.stream(messageRepo.findAll().spliterator(), false)
				.filter(message -> !emailMessage.getId().equals(message.getId()))
				.collect(Collectors.toList());
		
		//WHEN TEST REST
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders
						.put(ncAppConfigProperties.getContextPath() + "/delivery-info/messages/{messageId}/mark-as-read", Objects.requireNonNull(messages.get(0).getId()).toString())
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
		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> deliveryRepo.countByMessageIdAndStatus(emailMessage.getId(), READ) >= 1);
		List<DeliveryInfo> infosOfMessage = deliveryRepo.findByMessageIdAndStatus(emailMessage.getId(), READ);
		assertThat(infosOfMessage).hasSize(1);
	}

}
