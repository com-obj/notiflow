package com.obj.nc.repositories;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.refIntegrity.EntityExistenceChecker;
import org.springframework.data.repository.query.Param;

public interface GenericEventRepository extends PagingAndSortingRepository<GenericEvent, UUID>, EntityExistenceChecker<UUID> {
	
	GenericEvent findFirstByTimeConsumedIsNullOrderByTimeCreatedAsc();
	
	GenericEvent findByExternalId(String externalId);
	
	@Query("select * " +
			"from nc_event " +
			"where " +
			"	time_consumed between (:consumedFrom) and (:consumedTo) " +
			"and " +
			"	(:eventId)::uuid is null or id = (:eventId)::uuid")
	List<GenericEvent> findAllByTimeConsumedBetween(@Param("consumedFrom") Instant consumedFrom,
													@Param("consumedTo") Instant consumedTo,
													@Param("eventId") UUID eventId,
													Pageable pageable);
	
	@Query("select count(id) " +
			"from nc_event " +
			"where " +
			"	time_consumed between (:consumedFrom) and (:consumedTo) " +
			"and " +
			"	(:eventId)::uuid is null or id = (:eventId)::uuid")
	long countAllByTimeConsumedBetween(@Param("consumedFrom") Instant consumedFrom,
									   @Param("consumedTo") Instant consumedTo,
									   @Param("eventId") UUID eventId);

}
