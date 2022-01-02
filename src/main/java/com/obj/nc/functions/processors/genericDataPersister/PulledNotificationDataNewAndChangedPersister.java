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

package com.obj.nc.functions.processors.genericDataPersister;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.domain.genericData.GenericData;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.repositories.GenericDataRepository;
import com.obj.nc.utils.JsonUtils;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PulledNotificationDataNewAndChangedPersister extends ProcessorFunctionAdapter<List<JsonNode>, List<JsonNode>> {
    private final GenericDataRepository genericDataRepository;
    private final String externalIdAttrName;

    @Override
    protected Optional<PayloadValidationException> checkPreCondition(List<JsonNode> payload) {
        Optional<PayloadValidationException> exception = super.checkPreCondition(payload);
        if (exception.isPresent()) {
            return exception;
        }

        if (payload.stream().anyMatch(node -> GenericData.extractIdFromPayload(node, externalIdAttrName) == null)) {
            String template = "One of polled items is missing attribute with name %s. Set it in nc.data-sources.\"your-datasource\".externalId*";
            return Optional.of(new PayloadValidationException(String.format(template, externalIdAttrName)));
        }

        return Optional.empty();
    }

    @Override
    protected List<JsonNode> execute(List<JsonNode> incomingData) {
        List<GenericData> newAndChanged = findAndPersistAllNewAndChanged(incomingData);

        List<JsonNode> newAndChangedJsonNodes = newAndChanged.stream().map(data -> data.getBodyJson()).collect(Collectors.toList());
        
        return newAndChangedJsonNodes;
    }

    private List<GenericData> findAndPersistAllNewAndChanged(List<JsonNode> incomingData) {
        List<String> externalIds = incomingData.stream()
            .map(d-> GenericData.extractIdFromPayload(d, externalIdAttrName))
            .collect(Collectors.toList());
        
        List<GenericData> found = genericDataRepository.findAllHashesByExternalId(externalIds);

        List<GenericData> newAndChanged = collectNewAndChanged(incomingData, found);
        genericDataRepository.saveAll(newAndChanged);

        return newAndChanged;
    }

    private List<GenericData> collectNewAndChanged(List<JsonNode> incomingData, List<GenericData> savedData) {
        List<GenericData> newAndChanged = new ArrayList<>();

        for (JsonNode jsonNode : incomingData) {
            mapToGenericData(savedData, jsonNode)
                .ifPresent(newAndChanged::add);
        }
        return newAndChanged;
    }

    private Optional<GenericData> mapToGenericData(List<GenericData> found, JsonNode incomingData) {
        String externalId = GenericData.extractIdFromPayload(incomingData, externalIdAttrName);
        String incomingHash = GenericData.calculateHash(incomingData);    

        Optional<GenericData> record = getRecordByExternalId(found, externalId);

        if (record.isPresent()) {
            GenericData exitingData = record.get();
            String existingHash = exitingData.getHash();

            if (!existingHash.equals(incomingHash)) {
                //existing data, changed hash
                return Optional.of(exitingData.updateFromJson(incomingData));
            }

            //existing data, same hash,.. filter out
            return Optional.empty();
        } 
          
        //new data
        return Optional.of(
            GenericData.createFromJson(incomingData, externalIdAttrName)
        );
    }



    private Optional<GenericData> getRecordByExternalId(List<GenericData> savedData, String id) {
        return savedData.stream().filter(data -> id.equals(data.getExternalId())).findFirst();
    }
}
