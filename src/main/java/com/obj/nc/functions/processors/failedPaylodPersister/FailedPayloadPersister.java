/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.functions.processors.failedPaylodPersister;

import com.obj.nc.flows.errorHandling.domain.FailedPayload;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.repositories.FailedPayloadRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Log4j2
public class FailedPayloadPersister extends ProcessorFunctionAdapter<FailedPayload, FailedPayload> {

    @Autowired
    private FailedPayloadRepository failedPayloadRepo;


	@Override
	protected FailedPayload execute(FailedPayload failedPaylod) {
		try {
			return failedPayloadRepo.save(failedPaylod);
		} catch (Exception e) {
			log.error("Error ocurred in Error handling flow: ", e);
			throw e;
		}
	}

}
