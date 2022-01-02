/*
 * Copyright (C) 2021 the original author or authors.
 * This file is part of Notiflow
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.obj.nc.repositories;

import com.obj.nc.domain.pullNotifData.PullNotifDataPersistentState;
import com.obj.nc.domain.refIntegrity.EntityExistenceChecker;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PullNotifDataRepository extends CrudRepository<PullNotifDataPersistentState, UUID>, EntityExistenceChecker<UUID> {

    @Query(
        "SELECT id, external_id, time_created, hash " +
        "FROM nc_pulled_notif_data where external_id in (:ids)")
    List<PullNotifDataPersistentState> findAllHashesByExternalId(@Param("ids") List<String> ids);
}
