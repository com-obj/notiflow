package com.obj.nc.repositories;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.refIntegrity.EntityExistenceChecker;

public interface GenericEventRepository extends PagingAndSortingRepository<GenericEvent, UUID>, EntityExistenceChecker<UUID> {
	
	GenericEvent findFirstByTimeConsumedIsNullOrderByTimeCreatedAsc();
	
	GenericEvent findByExternalId(String externalId);
	
	List<GenericEvent> findAllByTimeConsumedBetween(Instant consumedFrom, Instant consumedTo, Pageable pageable);
	
	long countAllByTimeConsumedBetween(Instant consumedFrom, Instant consumedTo);

}
