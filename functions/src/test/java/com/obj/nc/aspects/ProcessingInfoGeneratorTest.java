package com.obj.nc.aspects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromNotificationIntentProcessingFunction;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
public class ProcessingInfoGeneratorTest {

	@Autowired MessagesFromNotificationIntentProcessingFunction createMessagesFunction;
	@Autowired ValidateAndGenerateEventIdProcessingFunction generateEventfunction;
	
	@Test
	void testOneToNProcessor() {
		//GIVEN
		String INPUT_JSON_FILE = "events/direct_message.json";
		NotificationIntent notificationIntent = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, NotificationIntent.class);

		notificationIntent = generateEventfunction.apply(notificationIntent);
		//WHEN
		List<Message> result = createMessagesFunction.apply(notificationIntent);
		
		//THEN
		assertThat(result.size()).isEqualTo(3);
		
		Message message = result.get(0);
		Header header = message.getHeader();
		assertThat(header.getFlowId()).isEqualTo(notificationIntent.getHeader().getFlowId());
		assertThat(header.getAttributes())
			.contains(
					entry("custom-proerty1", Arrays.asList("xx","yy")), 
					entry("custom-proerty2", "zz")
			);
		assertThat(header.getId()).isNotEqualTo(notificationIntent.getHeader().getId());
	}
}
