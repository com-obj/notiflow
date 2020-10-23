package com.obj.nc;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
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

import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.message.Message;
import com.obj.nc.utils.JsonUtils;

public class IntegrationTests {

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
			
			Event event = JsonUtils.readObjectFromClassPathResource("allEvents/ba_job_post.json", Event.class);
			org.springframework.messaging.Message<Event> inputEvent = convertBeanToMessagePayload(ctx, event);

			source.send(inputEvent);


			org.springframework.messaging.Message<byte[]> payload = target.receive(10000,"send-message.destination");

			Message message = convertMessagePayloadToBean(ctx, payload, Message.class);
			
            MatcherAssert.assertThat(message, CoreMatchers.notNullValue());
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
