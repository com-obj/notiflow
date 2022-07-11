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

package com.obj.nc.functions.processors.endpointPersister;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.repositories.EndpointsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EndpointPersister extends ProcessorFunctionAdapter<BasePayload<?>, BasePayload<?>> {
    private final EndpointsRepository repo;

    @Override
    protected BasePayload<?> execute(BasePayload<?> basePayload) {
        List<? extends ReceivingEndpoint> savedEndpoints = repo.upsertEndpoints(basePayload.getReceivingEndpoints());
        
        // //TODO: this should be removed as soon as delivery options are persisted 
        // for (ReceivingEndpoint endpointBeforeSave : basePayload.getReceivingEndpoints()) {
        //     if (endpointBeforeSave.getDeliveryOptions() == null) {
        //         continue;
        //     }

        //     ReceivingEndpoint endpoint = savedEndpoints.stream()
        //             .filter(endpointBeforeSave::equals)
        //             .findFirst()
        //             .orElseThrow(() -> new IllegalStateException("Failed to match endpoint after save: " + endpointBeforeSave));
        //     endpoint.setDeliveryOptions(endpointBeforeSave.getDeliveryOptions());
        // }
        
        basePayload.setReceivingEndpoints(savedEndpoints);

        return basePayload;
    }
}
