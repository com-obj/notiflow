package com.obj.nc.repositories;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.obj.nc.domain.event.GenericEvent;

public interface GenericEventRepository extends CrudRepository<GenericEvent, UUID> {
	
	public GenericEvent findFirstByTimeConsumedIsNullOrderByTimeCreatedAsc();
	
	public GenericEvent findByExternalId(String externalId);

}
