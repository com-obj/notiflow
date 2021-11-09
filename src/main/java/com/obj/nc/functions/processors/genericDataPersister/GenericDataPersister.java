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
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.repositories.GenericDataRepository;
import lombok.RequiredArgsConstructor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GenericDataPersister extends ProcessorFunctionAdapter<List<JsonNode>, List<JsonNode>> {
    private final GenericDataRepository genericDataRepository;
    private final String externalId;

    @Override
    protected List<JsonNode> execute(List<JsonNode> newData) {
        List<String> collectedIds = newData.stream().map(this::findIdByKey).collect(Collectors.toList());
        List<GenericData> found = genericDataRepository.findAllHashesByExternalId(collectedIds);

        genericDataRepository.saveAll(processData(newData, found));
        return newData;
    }

    private List<GenericData> processData(List<JsonNode> payload, List<GenericData> savedData) {
        List<GenericData> newData = new ArrayList<>();

        for (JsonNode jsonNode : payload) {
            mapToGenericData(savedData, jsonNode).ifPresent(newData::add);
        }
        return newData;
    }

    private Optional<GenericData> mapToGenericData(List<GenericData> found, JsonNode jsonNode) {
        String id = findIdByKey(jsonNode);
        String json = jsonNode.toString();
        String hash = hash(json);

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

    private String findIdByKey(JsonNode jsonNode) {
        return jsonNode.get(externalId).asText();
    }

    private Optional<GenericData> findRecord(List<GenericData> savedData, String id) {
        return savedData.stream().filter(data -> id.equals(data.getExternalId())).findFirst();
    }

    private String hash(String content) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(sha256.digest(content.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 is not available", e);
        }

    }
}
