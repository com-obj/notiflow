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

import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.headers.ProcessingInfo;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.hamcrest.MatcherAssert.assertThat;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
public class ProcessingInfoRepositoryTest extends BaseIntegrationTest {

	@Autowired ProcessingInfoRepository infoRepository;
	@Autowired GenericEventRepository eventRepo;
	@Autowired JdbcTemplate jdbcTemplate;
	
	@Test
	public void testPersistingSingleInfo() {
		ProcessingInfo transientInfo = createSimpleProcessingInfo();
		
		infoRepository.save(transientInfo);
		
		Optional<ProcessingInfo> infoInDb = infoRepository.findById(transientInfo.getProcessingId());
		
		Assertions.assertThat(infoInDb.isPresent()).isTrue();
		assertCurrentIsExpected(infoInDb.get(), transientInfo);
	}	
	
	@Test
	public void testFindByEventIdsAndStepName() {
		//GIVEN
		ProcessingInfo transientInfo = createSimpleProcessingInfo();
		
		//IF
		infoRepository.save(transientInfo);
		
		//THEN
		List<ProcessingInfo> infosInDb = infoRepository.findByAnyEventIdAndStepName(transientInfo.getEventIds()[0], "stepName");
		
		Assertions.assertThat(infosInDb.size()).isEqualTo(1);
		ProcessingInfo persistedPI = infosInDb.iterator().next();
		
       assertCurrentIsExpected(persistedPI, transientInfo);
	}
	
	@Test
	public void testPersistingInvalidReference() {
		// WHEN
		final ProcessingInfo info2 = ProcessingInfo.builder()
				.processingId(UUID.randomUUID())
				.prevProcessingId(null)
				.stepName("stepName")
				.stepIndex(1)
				.timeProcessingStart(Instant.now())
				.timeProcessingEnd(Instant.now())
				.stepDurationMs(0)
				.eventIds(new UUID[] {UUID.randomUUID()})
				.build();
		
		// THEN
		Assertions.assertThatThrownBy(
				() -> infoRepository.save(info2))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("which cannot be found in the DB");
	}

	public static void assertCurrentIsExpected( ProcessingInfo current, ProcessingInfo expected) {
		assertThat(current.getProcessingId(), CoreMatchers.equalTo(expected.getProcessingId()));
		assertThat(current.getPrevProcessingId(), CoreMatchers.equalTo(expected.getPrevProcessingId()));
		assertThat(current.getDiffJson(), CoreMatchers.equalTo(expected.getDiffJson()));
		assertThat(current.getStepName(), CoreMatchers.equalTo(expected.getStepName()));
		assertThat(current.getPayloadJsonStart(), CoreMatchers.equalTo(expected.getPayloadJsonStart()));
		assertThat(current.getStepDurationMs() , CoreMatchers.equalTo(expected.getStepDurationMs()));		
		//different precission in java an postgres
		assertThat(current.getTimeProcessingStart().truncatedTo(ChronoUnit.SECONDS) , CoreMatchers.equalTo(expected.getTimeProcessingStart().truncatedTo(ChronoUnit.SECONDS)));		
		assertThat(current.getTimeProcessingEnd().truncatedTo(ChronoUnit.SECONDS) , CoreMatchers.equalTo(expected.getTimeProcessingEnd().truncatedTo(ChronoUnit.SECONDS)));		
		
		assertThat(current.getEventIds(), CoreMatchers.equalTo(expected.getEventIds()));
	}

	private ProcessingInfo createSimpleProcessingInfo() {
		GenericEvent event = GenericEventRepositoryTest.createProcessedEvent();
		UUID eventId1 = eventRepo.save(event).getId();
		GenericEvent event2 = GenericEventRepositoryTest.createProcessedEvent();
		UUID eventId2 = eventRepo.save(event2).getId();
		
		String INPUT_JSON_FILE = "intents/direct_message.json";
		String content = JsonUtils.readJsonStringFromClassPathResource(INPUT_JSON_FILE);

		ProcessingInfo info = ProcessingInfo.builder()
				.processingId(UUID.randomUUID())
				.prevProcessingId(null)
				.stepName("stepName")
				.stepIndex(1)
				.timeProcessingStart(Instant.now())
				.timeProcessingEnd(Instant.now())
				.stepDurationMs(0)
				.payloadJsonStart(content)
				.payloadJsonEnd(content)
				.eventIds(new UUID[] {eventId1, eventId2})
				.build();
		return info;
	}
}
