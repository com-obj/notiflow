package com.obj.nc.controllers;

import com.jayway.jsonpath.JsonPath;
import com.obj.nc.controllers.DeliveryInfoRestController.EndpointDeliveryInfoDto;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.MessagePersistantState;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;
import com.obj.nc.repositories.EndpointsRepository;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
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

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@AutoConfigureMockMvc
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
class EndpointsRestControllerTest extends BaseIntegrationTest {
	
	@Autowired private EndpointsRepository endpointRepo;
	@Autowired private MockMvc mockMvc;

    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
    	purgeNotifTables(jdbcTemplate);
    }
    
    @Test
    void testFindDeliveryInfosRest() throws Exception {
    	//GIVEN
    	EmailEndpoint email = EmailEndpoint.builder().email("john.doe@objectify.sk").build();
		endpointRepo.persistEnpointIfNotExists(email);
		SmsEndpoint phone = SmsEndpoint.builder().phone("+999999999999").build();
    	endpointRepo.persistEnpointIfNotExists(phone);
    	
    	//WHEN TEST REST
        ResultActions resp = mockMvc
        		.perform(MockMvcRequestBuilders.get("/endpoints")
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
	
}
