package com.obj.nc.repositories;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.obj.nc.domain.refIntegrity.EntityExistanceChecker;
import com.obj.nc.flows.errorHandling.domain.FailedPaylod;

public interface FailedPayloadRepository extends CrudRepository<FailedPaylod, UUID>, EntityExistanceChecker<UUID> {
	

}
