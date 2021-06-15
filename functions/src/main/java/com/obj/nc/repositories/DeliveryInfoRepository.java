package com.obj.nc.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;

public interface DeliveryInfoRepository extends CrudRepository<DeliveryInfo, UUID> {
	
	List<DeliveryInfo> findByEventIdOrderByProcessedOn(UUID eventId);
	
	List<DeliveryInfo> findByEndpointIdOrderByProcessedOn(String endpointId);
	
	List<DeliveryInfo> findByMessageIdOrderByProcessedOn(UUID messageId);

}
