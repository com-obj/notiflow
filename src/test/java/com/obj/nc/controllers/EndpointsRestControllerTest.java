package com.obj.nc.controllers;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.jayway.jsonpath.JsonPath;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.MessagePersistantState;
import com.obj.nc.domain.message.SmsMessageTemplated;
import com.obj.nc.flows.messageProcessing.MessageProcessingFlow;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.repositories.EndpointsRepository;
import com.obj.nc.repositories.MessageRepository;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.SENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@AutoConfigureMockMvc
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
class EndpointsRestControllerTest extends BaseIntegrationTest {
	
	@Autowired private EndpointsRepository endpointsRepository;
	@Autowired private DeliveryInfoRepository deliveryInfoRepository;
	@Autowired private MessageRepository messageRepository;
	@Autowired private MockMvc mockMvc;
	@Autowired private MessageProcessingFlow messageProcessingFlow;
	
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
    void testFindAllEndpoints() throws Exception {
    	//GIVEN
		persistTestEndpoints();
	
		//WHEN TEST REST
        ResultActions resp = mockMvc
        		.perform(MockMvcRequestBuilders.get("/endpoints")
                .contentType(APPLICATION_JSON_UTF8)
        		.accept(APPLICATION_JSON_UTF8))
        		.andDo(MockMvcResultHandlers.print());
        
        //THEN
		resp
        	.andExpect(status().is2xxSuccessful());
	
		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertThat(endpoints).hasSize(2);
		assertContainsEndpoint(endpoints, "EMAIL", "john.doe@objectify.sk", 0);
		assertContainsEndpoint(endpoints, "SMS", "+999999999999", 0);
	}
	
	@Test
	void testCountSentMessages() throws Exception {
		//GIVEN
		EmailMessage message = JsonUtils.readObjectFromClassPathResource("messages/simple_email_message.json", EmailMessage.class);
		message.getHeader().addMessageId(message.getId());
		endpointsRepository.persistEnpointIfNotExists(message.getRecievingEndpoints());
		messageRepository.save(message.toPersistantState());
		
		messageProcessingFlow.processMessage(message);
		awaitMessageAndDeliveryInfos(2);
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		//THEN
		resp
				.andExpect(status().is2xxSuccessful());
		
		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertThat(endpoints).hasSize(1);
		assertContainsEndpoint(endpoints, "EMAIL", "john.doe@objectify.sk", 1);
	}
	
	@Test
	void testFilterDateRangeEndpointsStartAndEnd() throws Exception {
		//GIVEN
		persistTestEndpointsAndTestDeliveryInfos();
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.param("startAt", Instant.now().minusSeconds(60).toString())
						.param("endAt", Instant.now().plusSeconds(60).toString())
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		//THEN
		resp
				.andExpect(status().is2xxSuccessful());
		
		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertThat(endpoints).hasSize(2);
		assertContainsEndpoint(endpoints, "EMAIL", "john.doe@objectify.sk", 1);
		assertContainsEndpoint(endpoints, "SMS", "0908186997", 1);
	}
	
	@Test
	void testFilterDateRangeEndpointsStartOnly() throws Exception {
		//GIVEN
		persistTestEndpointsAndTestDeliveryInfos();
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.param("startAt", Instant.now().minusSeconds(60).toString())
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		//THEN
		resp
				.andExpect(status().is2xxSuccessful());
		
		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertThat(endpoints).hasSize(2);
		assertContainsEndpoint(endpoints, "EMAIL", "john.doe@objectify.sk", 1);
		assertContainsEndpoint(endpoints, "SMS", "0908186997", 1);
	}
	
	@Test
	void testFilterDateRangeEndpointsEndOnly() throws Exception {
		//GIVEN
		persistTestEndpointsAndTestDeliveryInfos();
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.param("endAt", Instant.now().plusSeconds(60).toString())
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		//THEN
		resp
				.andExpect(status().is2xxSuccessful());
		
		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertThat(endpoints).hasSize(2);
		assertContainsEndpoint(endpoints, "EMAIL", "john.doe@objectify.sk", 1);
		assertContainsEndpoint(endpoints, "SMS", "0908186997", 1);
	}
	
	@Test
	void testFilterEndpointTypeSMS() throws Exception {
		//GIVEN
		persistTestEndpoints();
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.param("endpointType", "SMS")
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		//THEN
		resp
				.andExpect(status().is2xxSuccessful());
		
		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertThat(endpoints).hasSize(1);
		assertContainsEndpoint(endpoints, "SMS", "+999999999999", 0);
	}
	
	@Test
	void testFilterInvalidEndpointType() throws Exception {
		//GIVEN
		persistTestEndpoints();
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.param("endpointType", "INVALID123")
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		//THEN
		resp
				.andExpect(status().is5xxServerError());
	}
	
	@Test
	void testGetPage0Size20() throws Exception {
		// GIVEN
		persistNEmailEndpoints(19);
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.param("page", "0")
						.param("size", "20")
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertThat(endpoints).hasSize(19);
	}
	
	@Test
	void testGetPage0Size10() throws Exception {
    	// GIVEN
		persistNEmailEndpoints(19);
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.param("page", "0")
						.param("size", "10")
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertThat(endpoints).hasSize(10);
	}
	
	@Test
	void testGetPage1Size10() throws Exception {
		// GIVEN
		persistNEmailEndpoints(19);
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints")
						.param("page", "1")
						.param("size", "10")
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		List<LinkedHashMap<?, ?>> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
		assertThat(endpoints).hasSize(9);
	}
	
	private void persistTestEndpoints() {
		EmailEndpoint email = EmailEndpoint.builder().email("john.doe@objectify.sk").build();
		endpointsRepository.persistEnpointIfNotExists(email);
		
		SmsEndpoint phone = SmsEndpoint.builder().phone("+999999999999").build();
		endpointsRepository.persistEnpointIfNotExists(phone);
	}
	
	private void persistTestEndpointsAndTestDeliveryInfos() {
		EmailMessage emailMessage = JsonUtils.readObjectFromClassPathResource("messages/simple_email_message.json", EmailMessage.class);
		emailMessage.getHeader().addMessageId(emailMessage.getId());
		endpointsRepository.persistEnpointIfNotExists(emailMessage.getRecievingEndpoints());
		messageRepository.save(emailMessage.toPersistantState());
		
		messageProcessingFlow.processMessage(emailMessage);
		Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> deliveryInfoRepository.countByEndpointIdAndStatus(emailMessage.getRecievingEndpoints().get(0).getId(), SENT) == 1L);
		
		SmsMessageTemplated smsMessage = JsonUtils.readObjectFromClassPathResource("messages/templated/txt_template_message.json", SmsMessageTemplated.class);
		smsMessage.getHeader().addMessageId(smsMessage.getId());
		endpointsRepository.persistEnpointIfNotExists(smsMessage.getRecievingEndpoints());
		messageRepository.save(smsMessage.toPersistantState());
		
		messageProcessingFlow.processMessage(smsMessage);
		Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> deliveryInfoRepository.countByEndpointIdAndStatus(((SmsEndpoint) smsMessage.getRecievingEndpoints().get(0)).getId(), SENT) == 1L);
	}
	
	private void assertContainsEndpoint(List<LinkedHashMap<?, ?>> endpoints, String endpointType, String endpointName, long sentMessagesCount) {
		Optional<LinkedHashMap<?, ?>> emailDto = endpoints.stream().filter(endpoint -> endpointType.equals(endpoint.get("type"))).findFirst();
		assertThat(emailDto.get().get("name")).isEqualTo(endpointName);
		assertThat(Long.valueOf(emailDto.get().get("sentMessagesCount").toString())).isEqualTo(sentMessagesCount);
	}
	
	private void awaitMessageAndDeliveryInfos(int numberToWait) {
		MessagePersistantState sentMessage = waitForFirstMessage();
		
		Awaitility.await().atMost(Duration.ofSeconds(3)).until(
				() -> deliveryInfoRepository.findByMessageIdOrderByProcessedOn(sentMessage.getId()).size() >= numberToWait
		);
	}

	public MessagePersistantState waitForFirstMessage() {
		Awaitility.await().atMost(Duration.ofSeconds(3)).until(
				() -> messageRepository.findAll().iterator().next() != null
		);
		
		MessagePersistantState sentMessage = messageRepository.findAll().iterator().next();		
		return sentMessage;
	}
	
	private void persistNEmailEndpoints(int n) {
		for (int i = 0; i < n; i++) {
			String name = "john.doe";
			EmailEndpoint email = EmailEndpoint.builder().email(name + i + "@objectify.sk").build();
			endpointsRepository.persistEnpointIfNotExists(email);
		}
	}
	
}