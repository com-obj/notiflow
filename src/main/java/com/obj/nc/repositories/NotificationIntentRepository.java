package com.obj.nc.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.domain.refIntegrity.EntityExistenceChecker;

public interface NotificationIntentRepository extends CrudRepository<NotificationIntent, UUID>, EntityExistenceChecker<UUID> {

	List<NotificationIntent> findByIdIn(List<UUID> intentIds);

}
