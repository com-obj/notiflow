package com.obj.nc.repositories;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.obj.nc.domain.refIntegrity.EntityExistenceChecker;
import com.obj.nc.flows.errorHandling.domain.FailedPayload;

public interface FailedPayloadRepository extends CrudRepository<FailedPayload, UUID>, EntityExistenceChecker<UUID> {
	

}
