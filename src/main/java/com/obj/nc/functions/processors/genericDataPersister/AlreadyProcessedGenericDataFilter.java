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
import com.obj.nc.repositories.GenericDataRepository;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class AlreadyProcessedGenericDataFilter implements Predicate<JsonNode> {
    private final GenericDataRepository genericDataRepository;
    private final String externalIdAttrName;

    @Override
    public boolean test(JsonNode jsonNode) {
        String externalId = extractIdFromPayload(jsonNode);

        GenericData genericData = genericDataRepository.findByExternalId(externalId)
                .orElse(GenericData.builder().id(UUID.randomUUID()).externalId(externalId).build());

        return updateAndSave(genericData, jsonNode);
    }

    private String extractIdFromPayload(JsonNode jsonNode) {
        JsonNode attr = jsonNode.get(externalIdAttrName);

        if (attr == null) {
            String template = "One of polled items is missing attribute with name %s. Set it in nc.data-sources.\"your-datasource\".externalId*";
            throw new PayloadValidationException(String.format(template, externalIdAttrName));
        }

        return attr.asText();
    }

    private boolean updateAndSave(GenericData found, JsonNode jsonNode) {
        String json = jsonNode.toString();
        String hash = HashFunction.hash(json);
        boolean notSameHash = !hash.equals(found.getHash());

        if (notSameHash) {
            genericDataRepository.save(found.withBody(json).withHash(hash));
        }

        return notSameHash;
    }
}
