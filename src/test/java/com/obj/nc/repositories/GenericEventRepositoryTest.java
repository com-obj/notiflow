package com.obj.nc.repositories;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
public class GenericEventRepositoryTest {

	@Autowired GenericEventRepository eventRepository;
	
	@Test
	public void testPersistingSingleEvent() {
		//GIVEN
		String INPUT_JSON_FILE = "events/direct_message.json";
		String content = JsonUtils.readJsonStringFromClassPathResource(INPUT_JSON_FILE);
		
		GenericEvent event = GenericEvent.builder()
				.externalId(UUID.randomUUID().toString())
				.flowId("FLOW_ID")
				.id(UUID.randomUUID())
				.payloadJson(JsonUtils.readJsonNodeFromJSONString(content))
				.timeConsumed(Instant.now())
				.build();
		
		eventRepository.save(event);
		
		Optional<GenericEvent> savedEvent = eventRepository.findById(event.getId());
		
		Assertions.assertThat(savedEvent.isPresent()).isTrue();

	}
}
