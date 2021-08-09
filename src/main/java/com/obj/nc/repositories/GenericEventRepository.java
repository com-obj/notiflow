package com.obj.nc.repositories;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.refIntegrity.EntityExistanceChecker;

public interface GenericEventRepository extends CrudRepository<GenericEvent, UUID>, EntityExistanceChecker<UUID> {
	
	public GenericEvent findFirstByTimeConsumedIsNullOrderByTimeCreatedAsc();
	
	public GenericEvent findByExternalId(String externalId);

}
