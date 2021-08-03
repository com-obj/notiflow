package com.obj.nc.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo.DELIVERY_STATUS;

public interface DeliveryInfoRepository extends CrudRepository<DeliveryInfo, UUID> {
	
	List<DeliveryInfo> findByEventIdAndStatusOrderByProcessedOn(UUID eventId, DELIVERY_STATUS status);
	
	List<DeliveryInfo> findByEventIdOrderByProcessedOn(UUID eventId);
	
	List<DeliveryInfo> findByEndpointIdOrderByProcessedOn(UUID endpointId);
	
	long countByEndpointIdAndStatus(UUID endpointId, DELIVERY_STATUS status);
	
	List<DeliveryInfo> findByMessageIdOrderByProcessedOn(UUID messageId);

}
