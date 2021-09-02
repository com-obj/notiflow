package com.obj.nc.repositories;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.domain.refIntegrity.EntityExistanceChecker;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationIntentRepository extends CrudRepository<NotificationIntent, UUID>, EntityExistanceChecker<UUID> {

	List<NotificationIntent> findByIdIn(List<UUID> intentIds);

}
