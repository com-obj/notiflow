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

package com.obj.nc.functions.processors.pullNotifDataPersister;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.domain.pullNotifData.PullNotifDataPersistentState;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.repositories.PullNotifDataRepository;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PulledNotificationDataNewAndChangedPersister extends ProcessorFunctionAdapter<List<JsonNode>, List<JsonNode>> {
    private final PullNotifDataRepository pullNotifDataRepository;
    private final String externalIdAttrName;
    private final List<String> hashAttributes;

    public PulledNotificationDataNewAndChangedPersister(PullNotifDataRepository pullNotifDataRepository, String externalIdAttrName) {
        this(pullNotifDataRepository, externalIdAttrName, new ArrayList<>());
    }

    public PulledNotificationDataNewAndChangedPersister(PullNotifDataRepository pullNotifDataRepository, String externalIdAttrName, List<String> hashAttributes) {
        this.pullNotifDataRepository = pullNotifDataRepository;
        this.externalIdAttrName = externalIdAttrName;
        this.hashAttributes = hashAttributes;
    }

    @Override
    protected Optional<PayloadValidationException> checkPreCondition(List<JsonNode> payload) {
        Optional<PayloadValidationException> exception = super.checkPreCondition(payload);
        if (exception.isPresent()) {
            return exception;
        }

        if (payload.stream().anyMatch(node -> PullNotifDataPersistentState.extractIdFromPayload(node, externalIdAttrName) == null)) {
            String template = "One of polled items is missing attribute with name %s. Set it in nc.data-sources.\"your-datasource\".externalId*";
            return Optional.of(new PayloadValidationException(String.format(template, externalIdAttrName)));
        }

        return Optional.empty();
    }

    @Override
    protected List<JsonNode> execute(List<JsonNode> incomingData) {
        List<PullNotifDataPersistentState> newAndChanged = findAndPersistAllNewAndChanged(incomingData);

        List<JsonNode> newAndChangedJsonNodes = newAndChanged.stream().map(data -> data.getBodyJson()).collect(Collectors.toList());
        
        return newAndChangedJsonNodes;
    }

    private List<PullNotifDataPersistentState> findAndPersistAllNewAndChanged(List<JsonNode> incomingData) {
        List<String> externalIds = incomingData.stream()
            .map(d-> PullNotifDataPersistentState.extractIdFromPayload(d, externalIdAttrName))
            .collect(Collectors.toList());
        
        List<PullNotifDataPersistentState> found = pullNotifDataRepository.findAllHashesByExternalId(externalIds);

        List<PullNotifDataPersistentState> newAndChanged = collectNewAndChanged(incomingData, found);
        pullNotifDataRepository.saveAll(newAndChanged);

        return newAndChanged;
    }

    private List<PullNotifDataPersistentState> collectNewAndChanged(List<JsonNode> incomingData, List<PullNotifDataPersistentState> savedData) {
        List<PullNotifDataPersistentState> newAndChanged = new ArrayList<>();

        for (JsonNode jsonNode : incomingData) {
            mapToPullNotifData(savedData, jsonNode)
                .ifPresent(newAndChanged::add);
        }
        return newAndChanged;
    }

    private Optional<PullNotifDataPersistentState> mapToPullNotifData(List<PullNotifDataPersistentState> found, JsonNode incomingData) {
        String externalId = PullNotifDataPersistentState.extractIdFromPayload(incomingData, externalIdAttrName);
        String incomingHash = PullNotifDataPersistentState.calculateHash(incomingData, hashAttributes);

        Optional<PullNotifDataPersistentState> record = getRecordByExternalId(found, externalId);

        if (record.isPresent()) {
            PullNotifDataPersistentState exitingData = record.get();
            String existingHash = exitingData.getHash();

            if (!existingHash.equals(incomingHash)) {
                //existing data, changed hash
                return Optional.of(exitingData.updateFromJson(incomingData, hashAttributes));
            }

            //existing data, same hash,.. filter out
            return Optional.empty();
        } 
          
        //new data
        return Optional.of(
            PullNotifDataPersistentState.createFromJson(incomingData, externalIdAttrName, hashAttributes)
        );
    }



    private Optional<PullNotifDataPersistentState> getRecordByExternalId(List<PullNotifDataPersistentState> savedData, String id) {
        return savedData.stream().filter(data -> id.equals(data.getExternalId())).findFirst();
    }
}
