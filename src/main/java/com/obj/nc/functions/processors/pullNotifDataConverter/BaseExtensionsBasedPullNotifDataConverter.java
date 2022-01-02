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

package com.obj.nc.functions.processors.pullNotifDataConverter;

import com.obj.nc.converterExtensions.pullNotifData.PullNotifDataConverterExtension;
import com.obj.nc.domain.pullNotifData.PullNotifData;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public abstract class BaseExtensionsBasedPullNotifDataConverter<OUT> extends ProcessorFunctionAdapter<PullNotifData<?>, List<OUT>> {
    
    public abstract List<? extends PullNotifDataConverterExtension<?, OUT>> getConverterExtensions();
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(PullNotifData<?> pullNotifData) {
        if (pullNotifData == null) {
            return Optional.of(new PayloadValidationException("PullNotifData instance must not be null"));
        }
        
        return Optional.empty();
    }
    
    private List<PullNotifDataConverterExtension<?, OUT>> findMatchingConverters(PullNotifData pullNotifData) {
        List<PullNotifDataConverterExtension<?, OUT>> matchingProcessors = new ArrayList<>();
        
        for (PullNotifDataConverterExtension<?, OUT> p: getConverterExtensions()) {

            if (pullNotifData.getPayloads().size() == 0) {
                continue;
            }

            Class<? extends Object> payloadCls = pullNotifData.getPayloads().get(0).getClass();
            if (!p.getPayloadType().isAssignableFrom(payloadCls)) {
                continue;
            }

            Optional<PayloadValidationException> errors = p.canHandle(pullNotifData);
            if (!errors.isPresent()) {
                matchingProcessors.add(p);
                continue;
            }

            if (log.isDebugEnabled()) {
                log.debug("BaseExtensionsBasedPullNotifDataConverter examined generic data processor which cannot handle payload " + pullNotifData + ". Processor replied" + errors.get().getMessage());
            }
 
        }
        
        return matchingProcessors;
    }
    
    @Override
    protected List<OUT> execute(PullNotifData pullNotifData) {
        return
                (List<OUT>)findMatchingConverters(pullNotifData)
                        .stream()
                        .flatMap(p -> p.convert(pullNotifData).stream())
                        .collect(Collectors.toList());
    }
    
}
