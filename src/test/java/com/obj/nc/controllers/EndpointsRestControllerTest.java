package com.obj.nc.controllers;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.jayway.jsonpath.JsonPath;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.MessagePersistantState;
import com.obj.nc.flows.messageProcessing.MessageProcessingFlow;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.repositories.EndpointsRepository;
import com.obj.nc.repositories.MessageRepository;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import org.awaitility.Awaitility;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import java.util.List;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@AutoConfigureMockMvc
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
class EndpointsRestControllerTest extends BaseIntegrationTest {
	
	@Autowired private EndpointsRepository endpointRepository;
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
    	EmailEndpoint email = EmailEndpoint.builder().email("john.doe@objectify.sk").build();
		endpointRepository.persistEnpointIfNotExists(email);
		SmsEndpoint phone = SmsEndpoint.builder().phone("+999999999999").build();
    	endpointRepository.persistEnpointIfNotExists(phone);
    	
    	//WHEN TEST REST
        ResultActions resp = mockMvc
        		.perform(MockMvcRequestBuilders.get("/endpoints/all")
                .contentType(APPLICATION_JSON_UTF8)
        		.accept(APPLICATION_JSON_UTF8))
        		.andDo(MockMvcResultHandlers.print());
        
        //THEN
		resp
        	.andExpect(status().is2xxSuccessful())
				
			.andExpect(jsonPath("$[0].id").value(CoreMatchers.is(email.getId().toString())))
			.andExpect(jsonPath("$[0].type").value(CoreMatchers.is("EMAIL")))
			.andExpect(jsonPath("$[0].name").value(CoreMatchers.is("john.doe@objectify.sk")))
				
			.andExpect(jsonPath("$[1].id").value(CoreMatchers.is(phone.getId().toString())))
			.andExpect(jsonPath("$[1].type").value(CoreMatchers.is("SMS")))
			.andExpect(jsonPath("$[1].name").value(CoreMatchers.is("+999999999999")));
	
		List<EndpointsRestController.EndpointDto> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$");
		assertThat(endpoints).hasSize(2);
	}
	
	@Test
	void testCountMessagesPerStatus() throws Exception {
		//GIVEN
		EmailMessage message = JsonUtils.readObjectFromClassPathResource("messages/simple_email_message.json", EmailMessage.class);
		messageProcessingFlow.processMessage(message);
		awaitDeliveryInfos();
		
		//WHEN
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/endpoints/all")
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		//THEN
		resp
				.andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$[0].infosPerStatus[0].status").value(CoreMatchers.is("SENT")))
				.andExpect(jsonPath("$[0].infosPerStatus[0].count").value(CoreMatchers.is(1)));
				
		List<EndpointsRestController.EndpointDto> endpoints = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$");
		assertThat(endpoints).hasSize(1);
	}
	
	private void awaitDeliveryInfos() {
		Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> messageRepository.findAll().iterator().next() != null);
		MessagePersistantState sentMessage = messageRepository.findAll().iterator().next();
		Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> deliveryInfoRepository.findByMessageIdOrderByProcessedOn(sentMessage.getId()).size() == 2);
	}
	
}
