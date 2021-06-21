package com.obj.nc.controllers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.controllers.DeliveryInfoRestController.EndpointDeliveryInfoDto;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.functions.sink.deliveryInfoPersister.domain.DeliveryInfo;
import com.obj.nc.functions.sink.deliveryInfoPersister.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.repositories.DeliveryInfoRepository;
import com.obj.nc.repositories.EndpointsRepository;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@AutoConfigureMockMvc
@SpringBootTest
class DeliveryInfoTest extends BaseIntegrationTest {
    
    
	@Autowired private DeliveryInfoRepository deliveryRepo;
	@Autowired private EndpointsRepository endpointRepo;
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
    	SmsEndpoint sms1 = SmsEndpoint.builder().phone("0908111111").build();
    	endpointRepo.persistEnpointIfNotExists(email1, sms1);
    	
    	//AND
    	UUID eventId = UUID.randomUUID();

    	DeliveryInfo info1 = DeliveryInfo.builder()
    			.endpointId(email1.getEndpointId()).eventId(eventId).status(DELIVERY_STATUS.PROCESSING).id(UUID.randomUUID()).build();
    	DeliveryInfo info2 = DeliveryInfo.builder()
    			.endpointId(email1.getEndpointId()).eventId(eventId).status(DELIVERY_STATUS.SENT).id(UUID.randomUUID()).build();


    	DeliveryInfo info3 = DeliveryInfo.builder()
    			.endpointId(sms1.getEndpointId()).eventId(eventId).status(DELIVERY_STATUS.PROCESSING).id(UUID.randomUUID()).build();
    	Thread.sleep(10); // to have different processedOn

    	DeliveryInfo info4 = DeliveryInfo.builder()
    			.endpointId(sms1.getEndpointId()).eventId(eventId).status(DELIVERY_STATUS.SENT).id(UUID.randomUUID()).build();
 
    	deliveryRepo.saveAll( Arrays.asList(info1, info2, info3, info4) );
    	
    	//WHEN
    	List<EndpointDeliveryInfoDto> infos = controller.findDeliveryInfosByEventId(eventId.toString());
    	
    	//THEN
    	Assertions.assertThat(infos.size()).isEqualTo(2);
    	EndpointDeliveryInfoDto infoForEmail = infos.stream().filter(i-> i.getEndpoint() instanceof EmailEndpoint).findFirst().get();

    	Instant now = Instant.now();

    	Assertions.assertThat(infoForEmail.endpointId).isEqualTo("jancuzy@gmail.com");
		Assertions.assertThat(infoForEmail.getStatusReachedAt()).isCloseTo(now, Assertions.within(1, ChronoUnit.MINUTES));
    	Assertions.assertThat(infoForEmail.getCurrentStatus()).isEqualTo(DELIVERY_STATUS.SENT);
    	infos.remove(infoForEmail);
    	
    	
    	EndpointDeliveryInfoDto infoForSms = infos.iterator().next();
    	Assertions.assertThat(infoForSms.endpointId).isEqualTo("0908111111");
    	Assertions.assertThat(infoForSms.getStatusReachedAt()).isCloseTo(now, Assertions.within(1, ChronoUnit.MINUTES));
    	Assertions.assertThat(infoForSms.getCurrentStatus()).isEqualTo(DELIVERY_STATUS.SENT);
    	
    }
    
    @Test
    void testFindDeliveryInfosRest() throws Exception {
    	//GIVEN
    	EmailEndpoint email1 = EmailEndpoint.builder().email("jancuzy@gmail.com").build();
    	endpointRepo.persistEnpointIfNotExists(email1);
    	
    	//AND
    	UUID eventId = UUID.randomUUID();
    	DeliveryInfo info1 = DeliveryInfo.builder()
    			.endpointId(email1.getEndpointId()).eventId(eventId).status(DELIVERY_STATUS.PROCESSING).id(UUID.randomUUID()).build();
    	DeliveryInfo info2 = DeliveryInfo.builder()
    			.endpointId(email1.getEndpointId()).eventId(eventId).status(DELIVERY_STATUS.SENT).id(UUID.randomUUID()).build();

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
			.andExpect(jsonPath("$[0].statusReachedAt").value(CoreMatchers.notNullValue()))
			.andExpect(jsonPath("$[0].endpoint.email").value(CoreMatchers.is("jancuzy@gmail.com")))
			.andExpect(jsonPath("$[0].endpoint.endpointId").value(CoreMatchers.is("jancuzy@gmail.com")))
			.andExpect(jsonPath("$[0].endpoint.@type").value(CoreMatchers.is("EMAIL")));
    }

}
