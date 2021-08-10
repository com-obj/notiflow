package com.obj.nc.repositories;

import com.obj.nc.domain.endpoints.RecievingEndpoint;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface EndpointsRepository extends PagingAndSortingRepository<RecievingEndpoint, UUID>, EndpointsRepositoryCustom {
}
