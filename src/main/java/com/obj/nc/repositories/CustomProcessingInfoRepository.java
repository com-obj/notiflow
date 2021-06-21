package com.obj.nc.repositories;

import java.util.List;
import java.util.UUID;

import com.obj.nc.domain.headers.ProcessingInfo;

public interface CustomProcessingInfoRepository {
	
	List<ProcessingInfo> findByAnyEventIdAndStepName(UUID eventId, String stepName);

}
