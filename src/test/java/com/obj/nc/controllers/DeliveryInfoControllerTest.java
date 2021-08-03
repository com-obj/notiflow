package com.obj.nc.controllers;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.jayway.jsonpath.JsonPath;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.MessagePersistantState;
import com.obj.nc.repositories.MessageRepository;
import com.obj.nc.services.EndpointsService;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.controllers.DeliveryInfoRestController.EndpointDeliveryInfoDto;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.repositories.DeliveryInfoRepository;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@AutoConfigureMockMvc
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
class DeliveryInfoControllerTest extends BaseIntegrationTest {
    
    
	@Autowired private DeliveryInfoRepository deliveryRepo;
	@Autowired private MessageRepository messageRepo;
	@Autowired private EndpointsService endpointService;
	@Autowired protected MockMvc mockMvc;
	@Autowired private DeliveryInfoRestController controller;

    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
    	purgeNotifTables(jdbcTemplate);
    }
    
    @Test
    void testFindDeliveryInfos() throws Exception {
    	//GIVEN
    	EmailEndpoint email1 = EmailEndpoint.builder().email("jancuzy@gmail.com").build();
		endpointService.persistEndpointIfNotExists(email1);
		SmsEndpoint sms1 = SmsEndpoint.builder().phone("0908111111").build();
    	endpointService.persistEndpointIfNotExists(sms1);
    	
    	//AND
    	UUID eventId = UUID.randomUUID();

    	DeliveryInfo info1 = DeliveryInfo.builder()
    			.endpointId(email1.getId()).eventId(eventId).status(DELIVERY_STATUS.PROCESSING).id(UUID.randomUUID()).build();
    	DeliveryInfo info2 = DeliveryInfo.builder()
    			.endpointId(email1.getId()).eventId(eventId).status(DELIVERY_STATUS.SENT).id(UUID.randomUUID()).build();


    	DeliveryInfo info3 = DeliveryInfo.builder()
    			.endpointId(sms1.getId()).eventId(eventId).status(DELIVERY_STATUS.PROCESSING).id(UUID.randomUUID()).build();
    	Thread.sleep(10); // to have different processedOn

    	DeliveryInfo info4 = DeliveryInfo.builder()
    			.endpointId(sms1.getId()).eventId(eventId).status(DELIVERY_STATUS.SENT).id(UUID.randomUUID()).build();
 
    	deliveryRepo.saveAll( Arrays.asList(info1, info2, info3, info4) );
    	
    	//WHEN
    	List<EndpointDeliveryInfoDto> infos = controller.findDeliveryInfosByEventId(eventId.toString(), null);
    	
    	//THEN
    	Assertions.assertThat(infos.size()).isEqualTo(2);
    	EndpointDeliveryInfoDto infoForEmail = infos.stream().filter(i-> i.getEndpoint() instanceof EmailEndpoint).findFirst().get();

    	Instant now = Instant.now();

    	Assertions.assertThat(infoForEmail.endpointId).isEqualTo(email1.getId());
		Assertions.assertThat(infoForEmail.getStatusReachedAt()).isCloseTo(now, Assertions.within(1, ChronoUnit.MINUTES));
    	Assertions.assertThat(infoForEmail.getCurrentStatus()).isEqualTo(DELIVERY_STATUS.SENT);
    	infos.remove(infoForEmail);
    	
    	
    	EndpointDeliveryInfoDto infoForSms = infos.iterator().next();
    	Assertions.assertThat(infoForSms.endpointId).isEqualTo(sms1.getId());
    	Assertions.assertThat(infoForSms.getStatusReachedAt()).isCloseTo(now, Assertions.within(1, ChronoUnit.MINUTES));
    	Assertions.assertThat(infoForSms.getCurrentStatus()).isEqualTo(DELIVERY_STATUS.SENT);
    	
    }
    
    @Test
    void testFindDeliveryInfosRest() throws Exception {
    	//GIVEN
    	EmailEndpoint email1 = EmailEndpoint.builder().email("jancuzy@gmail.com").build();
    	endpointService.persistEndpointIfNotExists(email1);
    	
    	//AND
    	UUID eventId = UUID.randomUUID();
    	DeliveryInfo info1 = DeliveryInfo.builder()
    			.endpointId(email1.getId()).eventId(eventId).status(DELIVERY_STATUS.PROCESSING).id(UUID.randomUUID()).build();
    	DeliveryInfo info2 = DeliveryInfo.builder()
    			.endpointId(email1.getId()).eventId(eventId).status(DELIVERY_STATUS.SENT).id(UUID.randomUUID()).build();

    	deliveryRepo.saveAll( Arrays.asList(info1, info2) );
    	
    	//WHEN TEST REST
        ResultActions resp = mockMvc
        		.perform(MockMvcRequestBuilders.get("/delivery-info/events/{eventId}",eventId.toString())
                .contentType(APPLICATION_JSON_UTF8)
        		.accept(APPLICATION_JSON_UTF8))
        		.andDo(MockMvcResultHandlers.print());
        
        //THEN
		
		resp
        	.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$[0].currentStatus").value(CoreMatchers.is("SENT")))
			.andExpect(jsonPath("$[0].endpoint.email").value(CoreMatchers.is("jancuzy@gmail.com")))
			.andExpect(jsonPath("$[0].endpoint.endpointId").value(CoreMatchers.is("jancuzy@gmail.com")))
			.andExpect(jsonPath("$[0].endpoint.@type").value(CoreMatchers.is("EMAIL"))).andReturn();
	
		String statusReachedAt = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$[0].statusReachedAt");
		SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm'Z'");
		Assertions.assertThat(ISO8601DATEFORMAT.parse(statusReachedAt)).isNotNull();
	}
	
	@Test
	void testReadMessageDeliveryInfoUpdate() throws Exception {
		//GIVEN
		EmailEndpoint email1 = EmailEndpoint.builder().email("jancuzy@gmail.com").build();
		endpointService.persistEndpointIfNotExists(email1);
		
		//AND
		EmailMessage emailMessage = new EmailMessage();
		emailMessage.setRecievingEndpoints(Arrays.asList(email1));
		emailMessage.getHeader().setEventIds(Arrays.asList(UUID.randomUUID()));
		MessagePersistantState emailMessagePersisted = messageRepo.save(emailMessage.toPersistantState());
		
		DeliveryInfo info = DeliveryInfo.builder()
				.endpointId(email1.getId()).eventId(UUID.randomUUID()).status(DELIVERY_STATUS.SENT).id(UUID.randomUUID()).messageId(emailMessagePersisted.getId()).build();
		
		deliveryRepo.save(info);
		
		//WHEN TEST REST
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.put("/delivery-info/messages/read/{messageId}", emailMessagePersisted.getId().toString())
						.contentType(APPLICATION_JSON_UTF8)
						.accept(APPLICATION_JSON_UTF8))
				.andDo(MockMvcResultHandlers.print());
		
		//THEN REDIRECT TO IMAGE
		resp
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/resources/images/px.png"));
		
		//AND IMAGE IS FOUND
		resp = mockMvc
				.perform(MockMvcRequestBuilders.get("/resources/images/px.png"))
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
