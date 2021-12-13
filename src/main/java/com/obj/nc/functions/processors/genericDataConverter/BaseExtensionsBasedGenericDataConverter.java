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

package com.obj.nc.functions.processors.genericDataConverter;

import com.obj.nc.converterExtensions.genericData.GenericDataConverterExtension;
import com.obj.nc.domain.dataObject.PulledNotificationData;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public abstract class BaseExtensionsBasedGenericDataConverter<OUT> extends ProcessorFunctionAdapter<PulledNotificationData<?>, List<OUT>> {
    
    public abstract List<? extends GenericDataConverterExtension<?, OUT>> getConverterExtensions();
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(PulledNotificationData<?> genericData) {
        if (genericData == null) {
            return Optional.of(new PayloadValidationException("GenericData instance must not be null"));
        }
        
        return Optional.empty();
    }
    
    private List<GenericDataConverterExtension<?, OUT>> findMatchingConverters(PulledNotificationData genericData) {
        List<GenericDataConverterExtension<?, OUT>> matchingProcessors = new ArrayList<>();
        
        for (GenericDataConverterExtension<?, OUT> p: getConverterExtensions()) {

            if (genericData.getPayloads().size() == 0) {
                continue;
            }

            Class<? extends Object> payloadCls = genericData.getPayloads().get(0).getClass();
            if (!p.getPayloadType().isAssignableFrom(payloadCls)) {
                continue;
            }

            Optional<PayloadValidationException> errors = p.canHandle(genericData);
            if (!errors.isPresent()) {
                matchingProcessors.add(p);
                continue;
            }

            if (log.isDebugEnabled()) {
                log.debug("BaseExtensionsBasedGenericDataConverter examined generic data processor which cannot handle payload " + genericData + ". Processor replied" + errors.get().getMessage());
            }
 
        }
        
        return matchingProcessors;
    }
    
    @Override
    protected List<OUT> execute(PulledNotificationData genericData) {
        return
                (List<OUT>)findMatchingConverters(genericData)
                        .stream()
                        .flatMap(p -> p.convert(genericData).stream())
                        .collect(Collectors.toList());
    }
    
}
