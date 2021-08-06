package com.obj.nc.repositories;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
public class RecievingEndpointRepositoryTest extends BaseIntegrationTest {

	@Autowired EndpointsRepository endpointRepository;
	
    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
    	purgeNotifTables(jdbcTemplate);
    }
	
	@Test
	public void testEndpointWithReuse() {
    	//GIVEN
    	EmailEndpoint email1 = EmailEndpoint.builder().email("jancuzy@gmail.com").build();
    	SmsEndpoint sms1 = SmsEndpoint.builder().phone("0908111111").build();
    	
    	//WHEN
    	endpointRepository.persistEnpointIfNotExists(email1, sms1);
		
    	//THEN
		List<RecievingEndpoint> endpoints = endpointRepository.findByIds(email1.getId(), sms1.getId());		
		Assertions.assertThat(endpoints.size()).isEqualTo(2);
		
		//AND WHEN
    	EmailEndpoint email2 = EmailEndpoint.builder().email("jancuzy@gmail.com").build();
    	SmsEndpoint sms2 = SmsEndpoint.builder().phone("0908111112").build();
		
       	//WHEN
    	List<RecievingEndpoint> persisted = endpointRepository.persistEnpointIfNotExists(email2, sms2);
		
    	//THEN
		endpoints = endpointRepository.findByNameIds(email2.getEndpointId(), sms2.getEndpointId());		
		Assertions.assertThat(endpoints.size()).isEqualTo(2);
		
		RecievingEndpoint emailPersisted = endpointRepository.findByNameIds(email2.getEndpointId()).iterator().next();	

		Assertions.assertThat(emailPersisted.getId()).isEqualTo(email1.getId()); //this means second email was not inserted. Only reused	
		Assertions.assertThat(persisted.stream().anyMatch(e-> e.getId().equals(email1.getId()))).isTrue(); //email2 not inserted, service returned email1Id
		Assertions.assertThat(persisted.stream().anyMatch(e-> e.getId().equals(sms2.getId()))).isTrue(); //sms2 inserted

	}


}
