package com.obj.nc.repositories;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.obj.nc.domain.refIntegrity.EntityExistanceChecker;
import org.springframework.data.domain.Pageable;

import com.obj.nc.domain.event.GenericEvent;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface GenericEventRepository extends PagingAndSortingRepository<GenericEvent, UUID>, EntityExistanceChecker<UUID> {
	
	GenericEvent findFirstByTimeConsumedIsNullOrderByTimeCreatedAsc();
	
	GenericEvent findByExternalId(String externalId);
	
	List<GenericEvent> findAllByTimeConsumedBetween(Instant consumedFrom, Instant consumedTo, Pageable pageable);
	
	long countAllByTimeConsumedBetween(Instant consumedFrom, Instant consumedTo);

}
