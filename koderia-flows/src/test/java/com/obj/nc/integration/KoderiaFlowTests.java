package com.obj.nc.integration;

import com.obj.nc.KoderiaFlowsApplication;
import com.obj.nc.dto.EmitEventDto;
import com.obj.nc.utils.JsonUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.support.GenericMessage;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.util.Map;

import static com.obj.nc.functions.processors.KoderiaEventConverterExecution.ORIGINAL_EVENT_FIELD;

@Testcontainers
public class KoderiaFlowTests {

	public static final String FINAL_STEP_QUEUE_NAME = "send-message.destination";
	public static final String DOCKER_COMPOSE_PATH = "../docker-k7s/minimal-components/docker-compose.yml";

	@Container
	public static DockerComposeContainer<?> environment = new DockerComposeContainer<>(new File(DOCKER_COMPOSE_PATH))
			.withLocalCompose(true);

	@Test
	public void testJobPostKoderiaEventEmited() throws Exception {
		Class<?>[] appConfigurations = TestChannelBinderConfiguration.getCompleteConfiguration(KoderiaFlowsApplication.class);
		try (
				ConfigurableApplicationContext ctx = new SpringApplicationBuilder(appConfigurations)
						.profiles("test")
						.run()
		) {
			String INPUT_JSON_FILE = "koderia/create_request/blog_body.json";
			EmitEventDto emitEventDto = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, EmitEventDto.class);
			Map<String, Object> operand = emitEventDto.asMap();

			InputDestination source = ctx.getBean(InputDestination.class);
			OutputDestination target = ctx.getBean(OutputDestination.class);

			GenericMessage<EmitEventDto> inputMessage = new GenericMessage<>(emitEventDto);
			source.send(inputMessage);

			org.springframework.messaging.Message<byte[]> payload1 = target.receive(3000,FINAL_STEP_QUEUE_NAME);
			com.obj.nc.domain.message.Message message1 = convertMessagePayloadToBean(ctx, payload1, com.obj.nc.domain.message.Message.class);

			org.springframework.messaging.Message<byte[]> payload2 = target.receive(3000,FINAL_STEP_QUEUE_NAME);
			com.obj.nc.domain.message.Message message2 = convertMessagePayloadToBean(ctx, payload2, com.obj.nc.domain.message.Message.class);

			org.springframework.messaging.Message<byte[]> payload3 = target.receive(3000,FINAL_STEP_QUEUE_NAME);
			com.obj.nc.domain.message.Message message3 = convertMessagePayloadToBean(ctx, payload3, com.obj.nc.domain.message.Message.class);


			MatcherAssert.assertThat(message1, CoreMatchers.notNullValue());
			MatcherAssert.assertThat(message1.getBody().getMessage().getContent().getText(), Matchers.equalTo(emitEventDto.getText()));
			MatcherAssert.assertThat(message1.getBody().getMessage().getContent().getSubject(), Matchers.equalTo(emitEventDto.getSubject()));
			MatcherAssert.assertThat(message1.getBody().getMessage().getContent().getAttributes().get(ORIGINAL_EVENT_FIELD), Matchers.equalTo(operand));

			MatcherAssert.assertThat(message2, CoreMatchers.notNullValue());
			MatcherAssert.assertThat(message2.getBody().getMessage().getContent().getText(), Matchers.equalTo(emitEventDto.getText()));
			MatcherAssert.assertThat(message2.getBody().getMessage().getContent().getSubject(), Matchers.equalTo(emitEventDto.getSubject()));
			MatcherAssert.assertThat(message2.getBody().getMessage().getContent().getAttributes().get(ORIGINAL_EVENT_FIELD), Matchers.equalTo(operand));

			MatcherAssert.assertThat(message3, CoreMatchers.notNullValue());
			MatcherAssert.assertThat(message3.getBody().getMessage().getContent().getText(), Matchers.equalTo(emitEventDto.getText()));
			MatcherAssert.assertThat(message3.getBody().getMessage().getContent().getSubject(), Matchers.equalTo(emitEventDto.getSubject()));
			MatcherAssert.assertThat(message3.getBody().getMessage().getContent().getAttributes().get(ORIGINAL_EVENT_FIELD), Matchers.equalTo(operand));
		}
	}

	private <T> T convertMessagePayloadToBean(ConfigurableApplicationContext ctx, org.springframework.messaging.Message<byte[]> payload, Class<T> payloadClass) {
		final MessageConverter converter = ctx.getBean(CompositeMessageConverter.class);
		T pojo = (T) converter.fromMessage(payload, payloadClass);
		return pojo;
	}

}
