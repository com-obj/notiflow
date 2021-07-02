package com.obj.nc.repositories;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.config.SpringIntegration;
import com.obj.nc.flows.errorHandling.domain.FailedPaylod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
public class FailedPaylodRepositoryTest {

	@Autowired FailedPayloadRepository failedPaylaodRepo;
	@Autowired @Qualifier(SpringIntegration.OBJECT_MAPPER_FOR_SPRING_MESSAGES_BEAN_NAME) ObjectMapper jsonConverterForMessages;
	
	@Test
	public void testPersistingSingleInfo() {
		TestPayload payload = TestPayload.builder().content("some error content").build();
		Message<TestPayload> msg = MessageBuilder.withPayload(payload).build();
		FailedPaylod failedPayload = null;
		
	    Exception e = Assertions.assertThrows(NumberFormatException.class, () -> {
	        Integer.parseInt("1a");
	    });
		
			
		failedPayload = FailedPaylod.builder()
				.flowId("default-flow-id")
				.id(UUID.randomUUID())
				.messageJson( jsonConverterForMessages.valueToTree(msg) )
				.channelNameForRetry("channel")
				.build();
		failedPayload.setAttributesFromException(e);
		
		failedPaylaodRepo.save(failedPayload);
		
		Optional<FailedPaylod> oFailedPayloadInDb = failedPaylaodRepo.findById(failedPayload.getId());
		
		assertThat(oFailedPayloadInDb.isPresent()).isTrue();
		
		assertThat(oFailedPayloadInDb.get().getFlowId()).isEqualTo(failedPayload.getFlowId());
		assertThat(oFailedPayloadInDb.get().getTimeCreated()).isNotNull();
		assertThat(oFailedPayloadInDb.get().getMessageJson()).isEqualTo(failedPayload.getMessageJson());
		assertThat(oFailedPayloadInDb.get().getExceptionName()).isEqualTo(failedPayload.getExceptionName());
		assertThat(oFailedPayloadInDb.get().getErrorMessage()).isEqualTo(failedPayload.getErrorMessage());
		assertThat(oFailedPayloadInDb.get().getStackTrace()).isEqualTo(failedPayload.getStackTrace());
		assertThat(oFailedPayloadInDb.get().getRootCauseExceptionName()).isEqualTo(failedPayload.getRootCauseExceptionName());		
		assertThat(oFailedPayloadInDb.get().getChannelNameForRetry()).isEqualTo(failedPayload.getChannelNameForRetry());		
	}

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestPayload {
    	
    	private String content;
    }
}
