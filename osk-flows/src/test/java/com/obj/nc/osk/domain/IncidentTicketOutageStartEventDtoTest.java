package com.obj.nc.osk.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.obj.nc.osk.domain.IncidentTicketServiceOutageForCustomerDto.CustomerSegment;
import com.obj.nc.osk.functions.processors.eventConverter.config.NotifEventConverterConfigProperties;
import com.obj.nc.utils.JsonUtils;

public class IncidentTicketOutageStartEventDtoTest {
	
	@Test
	public void messagesCanBeNullTest() {
		IncidentTicketOutageStartEventDto outageStart = 
				IncidentTicketOutageStartEventDto.builder().build();
		
		NotifEventConverterConfigProperties props = NotifEventConverterConfigProperties.builder().build();
		outageStart.filterOutLAsNotInConfig(props);
		
		Assertions.assertThat(outageStart.getMessages()).isEmpty();
	}
	
	@Test
	public void messagesFromJsonCanBeNullTest() {
		IncidentTicketOutageStartEventDto outageStart = JsonUtils.readObjectFromJSONString(
				  "{  \"id\":111,"
				+ "   \"@type\": \"OUTAGE_START\","
				+ "   \"name\":\"Outage MME\" }", IncidentTicketOutageStartEventDto.class);
		
		NotifEventConverterConfigProperties props = NotifEventConverterConfigProperties.builder().build();

		outageStart.filterOutLAsNotInConfig(props);
		Assertions.assertThat(outageStart.getMessages()).isEmpty();
	}
	
	@Test
	public void allSMEsRemainsOnlyConfiguredLAsRemain() {
		IncidentTicketServiceOutageForCustomerDto m1 = 
				IncidentTicketServiceOutageForCustomerDto.builder().b2bLogin("login1").customerSegment(CustomerSegment.SME).build();
		IncidentTicketServiceOutageForCustomerDto m2 = 
				IncidentTicketServiceOutageForCustomerDto.builder().b2bLogin("login2").customerSegment(CustomerSegment.SME).build();
		IncidentTicketServiceOutageForCustomerDto m3 = 
				IncidentTicketServiceOutageForCustomerDto.builder().b2bLogin("login3").customerSegment(CustomerSegment.LA).build();
		IncidentTicketServiceOutageForCustomerDto m4 = 
				IncidentTicketServiceOutageForCustomerDto.builder().b2bLogin("login4").customerSegment(CustomerSegment.LA).build();
		
		IncidentTicketOutageStartEventDto outageStart = 
				IncidentTicketOutageStartEventDto.builder()
					.message(m1).message(m2).message(m3).message(m4)
				.build();

		NotifEventConverterConfigProperties props = 
				NotifEventConverterConfigProperties.builder().b2bLoginNotify("login3").build();
		outageStart.filterOutLAsNotInConfig(props);
		
		Assertions.assertThat(outageStart.getMessages().size()).isEqualTo(3);
	}

}
