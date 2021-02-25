package com.obj.nc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MessageConverter;

import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.message.Message;
import com.obj.nc.utils.JsonUtils;

public class IntegrationTests extends DevelFlowsIntegrationTest {

	private static final String FINAL_STEP_QUEUE_NAME = "send-message.destination";

	@Test
	public void testEventEmited() throws Exception {
		Class<?>[] appConfigurations = TestChannelBinderConfiguration.getCompleteConfiguration(EventGeneratorTestApplication.class);
		try (
				ConfigurableApplicationContext ctx = new SpringApplicationBuilder(appConfigurations)
				.profiles("test")
                .run()
                ) {
			InputDestination source = ctx.getBean(InputDestination.class);
			OutputDestination target = ctx.getBean(OutputDestination.class);
			
			String INPUT_JSON_FILE = "allEvents/ba_job_post.json";
			Event event = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Event.class);
			org.springframework.messaging.Message<Event> inputEvent = convertBeanToMessagePayload(ctx, event);

			source.send(inputEvent);

			org.springframework.messaging.Message<byte[]> payload1 = target.receive(3000,FINAL_STEP_QUEUE_NAME);
			Message message1 = convertMessagePayloadToBean(ctx, payload1, Message.class);
			org.springframework.messaging.Message<byte[]> payload2 = target.receive(3000,FINAL_STEP_QUEUE_NAME);
			Message message2 = convertMessagePayloadToBean(ctx, payload2, Message.class);
			org.springframework.messaging.Message<byte[]> payload3 = target.receive(3000,FINAL_STEP_QUEUE_NAME);
			Message message3 = convertMessagePayloadToBean(ctx, payload3, Message.class);
			
			
            MatcherAssert.assertThat(message1, CoreMatchers.notNullValue());
            MatcherAssert.assertThat(message2, CoreMatchers.notNullValue());
            MatcherAssert.assertThat(message3, CoreMatchers.notNullValue());
        }
		
	}

	@Test
	public void testProcessJournalingPersisted() {
		Class<?>[] appConfigurations = TestChannelBinderConfiguration.getCompleteConfiguration(EventGeneratorTestApplication.class);
		try (
				ConfigurableApplicationContext ctx = new SpringApplicationBuilder(appConfigurations)
						.profiles("test")
						.run()
		) {
			// given empty processing info table
			JdbcTemplate jdbcTemplate = ctx.getBean(JdbcTemplate.class);
			jdbcTemplate.execute("truncate table nc_processing_info");

			InputDestination source = ctx.getBean(InputDestination.class);
			OutputDestination target = ctx.getBean(OutputDestination.class);

			// and event
			String INPUT_JSON_FILE = "allEvents/ba_job_post.json";
			Event event = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Event.class);
			org.springframework.messaging.Message<Event> inputEvent = convertBeanToMessagePayload(ctx, event);

			// when event processed
			source.send(inputEvent);

			org.springframework.messaging.Message<byte[]> payload1 = target.receive(3000,FINAL_STEP_QUEUE_NAME);
			Message message1 = convertMessagePayloadToBean(ctx, payload1, Message.class);
			org.springframework.messaging.Message<byte[]> payload2 = target.receive(3000,FINAL_STEP_QUEUE_NAME);
			Message message2 = convertMessagePayloadToBean(ctx, payload2, Message.class);
			org.springframework.messaging.Message<byte[]> payload3 = target.receive(3000,FINAL_STEP_QUEUE_NAME);
			Message message3 = convertMessagePayloadToBean(ctx, payload3, Message.class);

			// then process should be journaled in database
			List<Map<String, Object>> journaledRows = jdbcTemplate.queryForList("select payload_type, payload_id, step_name from nc_processing_info");

			Map<String, Object> step0 = journaledRows.get(0);
			MatcherAssert.assertThat(step0.get("payload_type"), CoreMatchers.equalTo("EVENT"));
			MatcherAssert.assertThat(step0.get("step_name"), CoreMatchers.equalTo("ValidateAndGenerateEventId"));

			Map<String, Object> step1 = journaledRows.get(1);
			MatcherAssert.assertThat(step1.get("payload_type"), CoreMatchers.equalTo("EVENT"));
			MatcherAssert.assertThat(step1.get("step_name"), CoreMatchers.equalTo("FindRecepientsUsingKoderiaSubsription"));

			Map<String, Object> step2 = journaledRows.get(2);
			MatcherAssert.assertThat(step2.get("payload_id"), CoreMatchers.equalTo(message1.getHeader().getId()));
			MatcherAssert.assertThat(step2.get("step_name"), CoreMatchers.equalTo("CreateMessagesFromEvent"));

			Map<String, Object> step3 = journaledRows.get(3);
			MatcherAssert.assertThat(step3.get("payload_id"), CoreMatchers.equalTo(message1.getHeader().getId()));
			MatcherAssert.assertThat(step3.get("step_name"), CoreMatchers.equalTo("SendEmail"));

			Map<String, Object> step4 = journaledRows.get(4);
			MatcherAssert.assertThat(step4.get("payload_id"), CoreMatchers.equalTo(message2.getHeader().getId()));
			MatcherAssert.assertThat(step4.get("step_name"), CoreMatchers.equalTo("CreateMessagesFromEvent"));

			Map<String, Object> step5 = journaledRows.get(5);
			MatcherAssert.assertThat(step5.get("payload_id"), CoreMatchers.equalTo(message2.getHeader().getId()));
			MatcherAssert.assertThat(step5.get("step_name"), CoreMatchers.equalTo("SendEmail"));

			Map<String, Object> step6 = journaledRows.get(6);
			MatcherAssert.assertThat(step6.get("payload_id"), CoreMatchers.equalTo(message3.getHeader().getId()));
			MatcherAssert.assertThat(step6.get("step_name"), CoreMatchers.equalTo("CreateMessagesFromEvent"));

			Map<String, Object> step7 = journaledRows.get(7);
			MatcherAssert.assertThat(step7.get("payload_id"), CoreMatchers.equalTo(message3.getHeader().getId()));
			MatcherAssert.assertThat(step7.get("step_name"), CoreMatchers.equalTo("SendEmail"));
		}
	}

	private <T> T convertMessagePayloadToBean(ConfigurableApplicationContext ctx, org.springframework.messaging.Message<byte[]> payload, Class<T> payloadClass) {
		final MessageConverter converter = ctx.getBean(CompositeMessageConverter.class);
		T pojo = (T) converter.fromMessage(payload, payloadClass);
		return pojo;
	}
	
	public static <T> org.springframework.messaging.Message<T> convertBeanToMessagePayload(ApplicationContext ctx, T object) {
		final MessageConverter converter = ctx.getBean(CompositeMessageConverter.class);
		Map<String, Object> headers = new HashMap<>();
		headers.put("contentType", "application/json");
		MessageHeaders messageHeaders = new MessageHeaders(headers);
		org.springframework.messaging.Message<T> message = (org.springframework.messaging.Message<T>)converter.toMessage(object, messageHeaders);
		
		return message;
	}
}
