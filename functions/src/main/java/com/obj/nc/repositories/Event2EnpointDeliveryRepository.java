package com.obj.nc.repositories;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.obj.nc.domain.event.Event2EnpointDelivery;

public interface Event2EnpointDeliveryRepository extends CrudRepository<Event2EnpointDelivery, UUID> {

}
