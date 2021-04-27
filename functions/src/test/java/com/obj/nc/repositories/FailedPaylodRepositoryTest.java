package com.obj.nc.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.IsTypedJson;
import com.obj.nc.flows.errorHandling.domain.FailedPaylod;
import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
public class FailedPaylodRepositoryTest {

	@Autowired FailedPayloadRepository failedPaylaodRepo;
	
	@Test
	public void testPersistingSingleInfo() {
		TestPayload payload = TestPayload.builder().content("some error content").build();
		FailedPaylod failedPayload = null;
		
	    Exception e = Assertions.assertThrows(NumberFormatException.class, () -> {
	        Integer.parseInt("1a");
	    });
		
			
		failedPayload = FailedPaylod.builder()
				.flowId("default-flow-id")
				.id(UUID.randomUUID())
				.payloadJson(JsonUtils.writeObjectToJSONNode(payload))
				.exceptionName(e.getClass().getName())
				.errorMessage(e.getMessage())
				.stackTrace(ExceptionUtils.getStackTrace(e))
				.rootCauseExceptionName(ExceptionUtils.getRootCause(e).getClass().getName())
				.build();
		
		failedPaylaodRepo.save(failedPayload);
		
		Optional<FailedPaylod> oFailedPayloadInDb = failedPaylaodRepo.findById(failedPayload.getId());
		
		assertThat(oFailedPayloadInDb.isPresent()).isTrue();
		
		assertThat(oFailedPayloadInDb.get().getFlowId()).isEqualTo(failedPayload.getFlowId());
		assertThat(oFailedPayloadInDb.get().getPayloadJson()).isEqualTo(failedPayload.getPayloadJson());
		assertThat(oFailedPayloadInDb.get().getExceptionName()).isEqualTo(failedPayload.getExceptionName());
		assertThat(oFailedPayloadInDb.get().getErrorMessage()).isEqualTo(failedPayload.getErrorMessage());
		assertThat(oFailedPayloadInDb.get().getStackTrace()).isEqualTo(failedPayload.getStackTrace());
		assertThat(oFailedPayloadInDb.get().getRootCauseExceptionName()).isEqualTo(failedPayload.getRootCauseExceptionName());		
	}

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonTypeInfo(include = As.PROPERTY, use = Id.NAME)
    @JsonSubTypes({ 
    	@Type(value = TestPayload.class, name = "TYPE_NAME")})
    public static class TestPayload implements IsTypedJson {
    	
    	private String content;
    }
}
