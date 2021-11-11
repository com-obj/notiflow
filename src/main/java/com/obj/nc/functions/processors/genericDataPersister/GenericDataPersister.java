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
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GenericDataPersister extends ProcessorFunctionAdapter<List<JsonNode>, List<JsonNode>> {
    private final GenericDataRepository genericDataRepository;
    private final String externalIdAttrName;

    @Override
    protected Optional<PayloadValidationException> checkPreCondition(List<JsonNode> payload) {
        Optional<PayloadValidationException> exception = super.checkPreCondition(payload);
        if (exception.isPresent()) {
            return exception;
        }

        if (payload.stream().anyMatch(node -> extractIdFromPayload(node) == null)) {
            String template = "One of polled items is missing attribute with name %s. Set it in nc.data-sources.\"your-datasource\".externalId*";
            return Optional.of(new PayloadValidationException(String.format(template, externalIdAttrName)));
        }

        return Optional.empty();
    }

    @Override
    protected List<JsonNode> execute(List<JsonNode> newData) {
        List<String> collectedIds = newData.stream().map(this::extractIdFromPayload).collect(Collectors.toList());
        List<GenericData> found = genericDataRepository.findAllHashesByExternalId(collectedIds);

        genericDataRepository.saveAll(collectNewAndUpdated(newData, found));
        return newData;
    }

    private List<GenericData> collectNewAndUpdated(List<JsonNode> payload, List<GenericData> savedData) {
        List<GenericData> newData = new ArrayList<>();

        for (JsonNode jsonNode : payload) {
            mapToGenericData(savedData, jsonNode).ifPresent(newData::add);
        }
        return newData;
    }

    private Optional<GenericData> mapToGenericData(List<GenericData> found, JsonNode jsonNode) {
        String id = extractIdFromPayload(jsonNode);
        String json = jsonNode.toString();
        String hash = HashFunction.hash(json);

        Optional<GenericData> record = findRecord(found, id);

        if (record.isPresent()) {
            GenericData data = record.get();

            if (!data.getHash().equals(hash)) {
                return Optional.of(data.withBody(json).withHash(hash));
            }
        } else {
            return Optional.of(GenericData.builder().id(UUID.randomUUID()).externalId(id).body(json).hash(hash).build());
        }

        return Optional.empty();
    }

    private String extractIdFromPayload(JsonNode jsonNode) {
        JsonNode attr = jsonNode.get(externalIdAttrName);

        if (attr == null) {
            return null;
        }

        return attr.asText();
    }

    private Optional<GenericData> findRecord(List<GenericData> savedData, String id) {
        return savedData.stream().filter(data -> id.equals(data.getExternalId())).findFirst();
    }
}
