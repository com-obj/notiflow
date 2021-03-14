package com.obj.nc.repositories;

import org.springframework.data.repository.CrudRepository;

import com.obj.nc.domain.event.GenericEvent;

public interface GenericEventRepository extends CrudRepository<GenericEvent, String> {
	
	public GenericEvent findFirstByTimeConsumedIsNullOrderByTimeCreatedAsc();

}
