package com.obj.nc.osk.repositories;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.obj.nc.domain.event.GenericEvent;

public interface ExtendedGenericEventRepository extends CrudRepository<GenericEvent, UUID>, CustomGenericEventRepository {

}
