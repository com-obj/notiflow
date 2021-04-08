package com.obj.nc.osk.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.obj.nc.utils.JsonUtils;

public class IncidentTicketNotificationContactDtoTest {
	
	@Test
	public void emailAddressCanBeNull() {
		IncidentTicketNotificationContactDto contact = JsonUtils.readObjectFromJSONString(
				"{\"id\": 888,"
				+ "\"name\": \"Jozo\"}", IncidentTicketNotificationContactDto.class);
		
		Assertions.assertThat(contact.asEmailEnpoints()).isNotNull();
		Assertions.assertThat(contact.asEmailEnpoints()).isEmpty();
		
		Assertions.assertThat(contact.asEnpoints()).isNotNull();
		Assertions.assertThat(contact.asEnpoints()).isEmpty();
	}
	
	@Test
	public void phonesCanBeNull() {
		IncidentTicketNotificationContactDto contact = JsonUtils.readObjectFromJSONString(
				"{\"id\": 888,"
				+ "\"name\": \"Jozo\"}", IncidentTicketNotificationContactDto.class);
		
		Assertions.assertThat(contact.asSmsEnpoints()).isNotNull();
		Assertions.assertThat(contact.asSmsEnpoints()).isEmpty();
		
		Assertions.assertThat(contact.asEnpoints()).isNotNull();
		Assertions.assertThat(contact.asEnpoints()).isEmpty();
	}

}
