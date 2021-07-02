package com.obj.nc.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DeliveryInfoRepositoryTest {

	@Autowired DeliveryInfoRepository deliveryInfoRepo;
	
	@BeforeEach
	public void clean() {
		deliveryInfoRepo.deleteAll();
	}
	
	@Test
	public void testPersistingSingleInfo() {
		DeliveryInfo deliveryInfo = DeliveryInfo.builder()
				.endpointId(UUID.randomUUID())
				.eventId(UUID.randomUUID())
				.status(DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.build();
		
		deliveryInfoRepo.save(deliveryInfo);
		
		Optional<DeliveryInfo> infoInDb = deliveryInfoRepo.findById(deliveryInfo.getId());
		
		Assertions.assertThat(infoInDb.isPresent()).isTrue();
	}
	
	@Test
	public void testFindByEventId() {
		UUID eventId = UUID.randomUUID();
		DeliveryInfo deliveryInfo1 = DeliveryInfo.builder()
				.endpointId(UUID.randomUUID())
				.eventId(eventId)
				.status(DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.build();
		
		DeliveryInfo deliveryInfo2 = DeliveryInfo.builder()
				.endpointId(UUID.randomUUID())
				.eventId(eventId)
				.status(DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.build();
		
		DeliveryInfo deliveryInfo3 = DeliveryInfo.builder()
				.endpointId(UUID.randomUUID())
				.eventId(UUID.randomUUID())
				.status(DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.build();
		
		deliveryInfoRepo.save(deliveryInfo1);
		deliveryInfoRepo.save(deliveryInfo2);
		deliveryInfoRepo.save(deliveryInfo3);
		
		List<DeliveryInfo> infosInDb = deliveryInfoRepo.findByEventIdOrderByProcessedOn(eventId);
		
		Assertions.assertThat(infosInDb.size()).isEqualTo(2);
	}
	
	@Test
	public void testFindByEndpointId() {
		UUID endpointId = UUID.randomUUID();
		DeliveryInfo deliveryInfo1 = DeliveryInfo.builder()
				.endpointId(endpointId)
				.eventId(UUID.randomUUID())
				.status(DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.build();
		
		DeliveryInfo deliveryInfo2 = DeliveryInfo.builder()
				.endpointId(endpointId)
				.eventId(UUID.randomUUID())
				.status(DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.build();
		
		DeliveryInfo deliveryInfo3 = DeliveryInfo.builder()
				.endpointId(UUID.randomUUID())
				.eventId(UUID.randomUUID())
				.status(DELIVERY_STATUS.SENT)
				.id(UUID.randomUUID())
				.build();
		
		deliveryInfoRepo.save(deliveryInfo1);
		deliveryInfoRepo.save(deliveryInfo2);
		deliveryInfoRepo.save(deliveryInfo3);
		
		List<DeliveryInfo> infosInDb = deliveryInfoRepo.findByEndpointIdOrderByProcessedOn(endpointId);
		
		Assertions.assertThat(infosInDb.size()).isEqualTo(2);
	}

}
