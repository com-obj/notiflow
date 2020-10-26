package com.obj.nc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.testcontainers.containers.DockerComposeContainer;

import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.message.Message;
import com.obj.nc.utils.JsonUtils;

public class IntegrationTests {

	private static final String FINAL_STEP_QUEUE_NAME = "send-message.destination";
	
	@ClassRule
	public static DockerComposeContainer environment = new DockerComposeContainer(new File("../docker-k7s/minimal-components/docker-compose.yml"))
		.withLocalCompose(true);

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
			
			String INPUT_JSON_FILE = "events/ba_job_post.json";
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
