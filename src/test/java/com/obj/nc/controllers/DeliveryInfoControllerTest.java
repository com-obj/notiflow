package com.obj.nc.controllers;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.jayway.jsonpath.JsonPath;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessagePersistantState;
import com.obj.nc.flows.messageProcessing.MessageProcessingFlow;
import com.obj.nc.functions.processors.messageBuilder.MessageByRecipientTokenizer;
import com.obj.nc.repositories.*;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.hamcrest.CoreMatchers;
import org.hamcrest.text.MatchesPattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
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
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.READ;
import static com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS.SENT;
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
	@Autowired private EndpointsRepository endpointRepo;
	@Autowired protected MockMvc mockMvc;
	@Autowired private DeliveryInfoRestController controller;
	@Autowired GenericEventRepository eventRepo;
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
    void testFindEventDeliveryInfos() throws Exception {
    	//GIVEN
    	EmailEndpoint email1 = EmailEndpoint.builder().email("jancuzy@gmail.com").build();
    	SmsEndpoint sms1 = SmsEndpoint.builder().phone("0908111111").build();
    	Map<String, RecievingEndpoint> endpoints = endpointRepo.persistEnpointIfNotExistsMappedToNameId(email1, sms1);
    	UUID emailEndPointId = endpoints.get("jancuzy@gmail.com").getId();
    	UUID smsEndPointId = endpoints.get("0908111111").getId();
    	
    	//AND
		GenericEvent event = GenericEventRepositoryTest.createDirectMessageEvent();
		UUID eventId = eventRepo.save(event).getId();

    	//AND
    	DeliveryInfo info1 = DeliveryInfo.builder()
    			.endpointId(emailEndPointId).eventId(eventId).status(DELIVERY_STATUS.PROCESSING).id(UUID.randomUUID()).build();
    	DeliveryInfo info2 = DeliveryInfo.builder()
    			.endpointId(emailEndPointId).eventId(eventId).status(SENT).id(UUID.randomUUID()).build();


    	DeliveryInfo info3 = DeliveryInfo.builder()
    			.endpointId(smsEndPointId).eventId(eventId).status(DELIVERY_STATUS.PROCESSING).id(UUID.randomUUID()).build();
    	Thread.sleep(10); // to have different processedOn

    	DeliveryInfo info4 = DeliveryInfo.builder()
    			.endpointId(smsEndPointId).eventId(eventId).status(SENT).id(UUID.randomUUID()).build();
 
    	deliveryRepo.saveAll( Arrays.asList(info1, info2, info3, info4) );
    	
    	//WHEN
    	List<EndpointDeliveryInfoDto> infos = controller.findDeliveryInfosByEventId(eventId.toString());
    	
    	//THEN
    	Assertions.assertThat(infos.size()).isEqualTo(2);
    	EndpointDeliveryInfoDto infoForEmail = infos.stream().filter(i-> i.getEndpoint() instanceof EmailEndpoint).findFirst().get();

    	Instant now = Instant.now();

    	Assertions.assertThat(infoForEmail.endpointId).isEqualTo(emailEndPointId);
		Assertions.assertThat(infoForEmail.getStatusReachedAt()).isCloseTo(now, Assertions.within(1, ChronoUnit.MINUTES));
    	Assertions.assertThat(infoForEmail.getCurrentStatus()).isEqualTo(SENT);
    	infos.remove(infoForEmail);
    	
    	
    	EndpointDeliveryInfoDto infoForSms = infos.iterator().next();
    	Assertions.assertThat(infoForSms.endpointId).isEqualTo(smsEndPointId);
    	Assertions.assertThat(infoForSms.getStatusReachedAt()).isCloseTo(now, Assertions.within(1, ChronoUnit.MINUTES));
    	Assertions.assertThat(infoForSms.getCurrentStatus()).isEqualTo(SENT);
    	
    }
    
    @Test
    void testFindEventDeliveryInfosRest() throws Exception {
    	//GIVEN
    	EmailEndpoint email1 = EmailEndpoint.builder().email("jancuzy@gmail.com").build();
    	UUID emailEndpointId = endpointRepo.persistEnpointIfNotExists(email1).getId();
    	
    	//AND
		GenericEvent event = GenericEventRepositoryTest.createDirectMessageEvent();
		UUID eventId = eventRepo.save(event).getId();

    	//AND
    	DeliveryInfo info1 = DeliveryInfo.builder()
    			.endpointId(emailEndpointId).eventId(eventId).status(DELIVERY_STATUS.PROCESSING).id(UUID.randomUUID()).build();
    	DeliveryInfo info2 = DeliveryInfo.builder()
    			.endpointId(emailEndpointId).eventId(eventId).status(SENT).id(UUID.randomUUID()).build();

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
	void testFindMessageDeliveryInfos() {
		// GIVEN
		EmailEndpoint email1 = EmailEndpoint.builder().email("john.doe@gmail.com").build();
		email1 = endpointRepo.persistEnpointIfNotExists(email1);
		EmailEndpoint email2 = EmailEndpoint.builder().email("john.dudly@gmail.com").build();
		email2 = endpointRepo.persistEnpointIfNotExists(email2);
		
		//AND
		EmailMessage emailMessage = new EmailMessage();
		emailMessage.setBody(EmailContent.builder().subject("Subject").text("Text").build());
		emailMessage.setRecievingEndpoints(Arrays.asList(email1, email2));
		
		messageProcessingFlow.processMessage(emailMessage);
		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> deliveryRepo.countByMessageIdAndStatus(emailMessage.getId(), SENT) >= 2);
		
		// WHEN find infos by input message's id
		List<EndpointDeliveryInfoDto> deliveryInfosByMessageId = controller.findDeliveryInfosByMessageId(emailMessage.getId().toString());
		
		// THEN should find latest states of tokenized messages
		assertThat(deliveryInfosByMessageId)
				.hasSize(2)
				.allMatch(endpointDeliveryInfoDto -> SENT.equals(endpointDeliveryInfoDto.getCurrentStatus()));
		
		assertThat(deliveryInfosByMessageId)
				.filteredOn(endpointDeliveryInfoDto -> "john.doe@gmail.com".equals(endpointDeliveryInfoDto.getEndpoint().getEndpointId()))
				.isNotEmpty();
		
		assertThat(deliveryInfosByMessageId)
				.filteredOn(endpointDeliveryInfoDto -> "john.dudly@gmail.com".equals(endpointDeliveryInfoDto.getEndpoint().getEndpointId()))
				.isNotEmpty();
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
		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> deliveryRepo.countByMessageIdAndStatus(emailMessage.getId(), SENT) >= 2);
		
		List<MessagePersistantState> messages = StreamSupport.stream(messageRepo.findAll().spliterator(), false)
				.filter(message -> !emailMessage.getId().equals(message.getId()))
				.collect(Collectors.toList());
		
		//WHEN TEST REST
		ResultActions resp = mockMvc
				.perform(MockMvcRequestBuilders.put("/delivery-info/messages/{messageId}/mark-as-read", Objects.requireNonNull(messages.get(0).getId()).toString())
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
		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> deliveryRepo.countByMessageIdAndStatus(emailMessage.getId(), READ) >= 1);
		List<DeliveryInfo> infosOfMessage = deliveryRepo.findByMessageIdAndStatus(emailMessage.getId(), READ);
		assertThat(infosOfMessage).hasSize(1);
	}

}
