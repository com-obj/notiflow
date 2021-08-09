package com.obj.nc.repositories;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.obj.nc.domain.headers.ProcessingInfo;
import com.obj.nc.domain.refIntegrity.EntityExistanceChecker;

public interface ProcessingInfoRepository extends CrudRepository<ProcessingInfo, UUID>, CustomProcessingInfoRepository, EntityExistanceChecker<UUID> {

}
