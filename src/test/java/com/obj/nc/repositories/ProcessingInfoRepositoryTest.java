package com.obj.nc.repositories;

import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.headers.ProcessingInfo;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest
public class ProcessingInfoRepositoryTest {

	@Autowired ProcessingInfoRepository infoRepository;
	
	@Test
	public void testPersistingSingleInfo() {
		ProcessingInfo info = createSimpleProcessingInfo();
		
		infoRepository.save(info);
		
		Optional<ProcessingInfo> infoInDb = infoRepository.findById(info.getProcessingId());
		
		Assertions.assertThat(infoInDb.isPresent()).isTrue();
	}
	
	@Test
	@Disabled
	public void testFindByEventIdsAndStepName() {
		//GIVEN
		ProcessingInfo transientInfo = createSimpleProcessingInfo();
		
		infoRepository.save(transientInfo);
		
		List<ProcessingInfo> infosInDb = infoRepository.findByAnyEventIdAndStepName(transientInfo.getEventIds()[0], "stepName");
		
		Assertions.assertThat(infosInDb.size()).isEqualTo(1);
		ProcessingInfo persistedPI = infosInDb.iterator().next();
		
       assertCurrentIsExpected(transientInfo, persistedPI);
	}

	public static void assertCurrentIsExpected( ProcessingInfo current, ProcessingInfo expected) {
		assertThat(current.getProcessingId(), CoreMatchers.equalTo(expected.getProcessingId()));
		assertThat(current.getPrevProcessingId(), CoreMatchers.equalTo(expected.getPrevProcessingId()));
		assertThat(current.getDiffJson(), CoreMatchers.equalTo(expected.getDiffJson()));
		assertThat(current.getStepName(), CoreMatchers.equalTo(expected.getStepName()));
		assertThat(current.getPayloadJsonStart(), CoreMatchers.equalTo(expected.getPayloadJsonStart()));
		assertThat(current.getStepDurationMs(), CoreMatchers.equalTo(expected.getStepDurationMs()));
		assertThat(current.getTimeProcessingStart(), CoreMatchers.equalTo(expected.getTimeProcessingStart()));
		assertThat(current.getTimeProcessingEnd(), CoreMatchers.equalTo(expected.getTimeProcessingEnd()));
		assertThat(current.getEventIds(), CoreMatchers.equalTo(expected.getEventIds()));
	}

	private ProcessingInfo createSimpleProcessingInfo() {
		String INPUT_JSON_FILE = "events/direct_message.json";
		String content = JsonUtils.readJsonStringFromClassPathResource(INPUT_JSON_FILE);

		ProcessingInfo info = ProcessingInfo.builder()
				.processingId(UUID.randomUUID())
				.prevProcessingId(UUID.randomUUID())
				.stepName("stepName")
				.stepIndex(1)
				.timeProcessingStart(Instant.now())
				.timeProcessingEnd(Instant.now())
				.stepDurationMs(0)
				.payloadJsonStart(content)
				.payloadJsonEnd(content)
				.eventIds(new UUID[] {UUID.randomUUID(), UUID.randomUUID()})
				.build();
		return info;
	}
}
