/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.config.SpringIntegration;
import com.obj.nc.flows.errorHandling.domain.FailedPayload;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
public class FailedPaylodRepositoryTest extends BaseIntegrationTest {

	@Autowired FailedPayloadRepository failedPaylaodRepo;
	@Autowired @Qualifier(SpringIntegration.OBJECT_MAPPER_FOR_SPRING_MESSAGES_BEAN_NAME) ObjectMapper jsonConverterForMessages;
	
	@Test
	public void testPersistingSingleInfo() {
		TestPayload payload = TestPayload.builder().content("some error content").build();
		Message<TestPayload> msg = MessageBuilder.withPayload(payload).build();
		FailedPayload failedPayload = null;
		
	    Exception e = Assertions.assertThrows(NumberFormatException.class, () -> {
	        Integer.parseInt("1a");
	    });
		
			
		failedPayload = FailedPayload.builder()
				.flowId("default-flow-id")
				.id(UUID.randomUUID())
				.messageJson( jsonConverterForMessages.valueToTree(msg) )
				.channelNameForRetry("channel")
				.build();
		failedPayload.setAttributesFromException(e);
		
		failedPaylaodRepo.save(failedPayload);
		
		Optional<FailedPayload> oFailedPayloadInDb = failedPaylaodRepo.findById(failedPayload.getId());
		
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
