package com.obj.nc.repositories;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.obj.nc.domain.headers.ProcessingInfo;

public interface ProcessingInfoRepository extends CrudRepository<ProcessingInfo, UUID>, CustomProcessingInfoRepository {

}
