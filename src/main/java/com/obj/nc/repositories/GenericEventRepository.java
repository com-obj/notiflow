package com.obj.nc.repositories;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import com.obj.nc.domain.event.GenericEvent;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface GenericEventRepository extends PagingAndSortingRepository<GenericEvent, UUID> {
	
	GenericEvent findFirstByTimeConsumedIsNullOrderByTimeCreatedAsc();
	
	GenericEvent findByExternalId(String externalId);
	
	List<GenericEvent> findAllByTimeConsumedBetween(Instant consumedFrom, Instant consumedTo, Pageable pageable);
	
	long countAllByTimeConsumedBetween(Instant consumedFrom, Instant consumedTo);

}
