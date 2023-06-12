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

package com.obj.nc.domain.pullNotifData;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.obj.nc.exceptions.PayloadValidationException;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.utils.JsonUtils;


@Getter @Setter
@Table("nc_pulled_notif_data")
@RequiredArgsConstructor(staticName = "of")
public class PullNotifDataPersistentState implements Persistable<UUID> {
    @Id
    private UUID id;

    private String externalId;

    @CreatedDate
    private Instant timeCreated;

    @Column("payload_json")
    private String body;

    @Transient
    private JsonNode bodyJson;

    private String hash;

    @Builder
    PullNotifDataPersistentState(UUID id, String externalId, Instant timeCreated, String body, String hash) {
        this.id = id;
        this.externalId = externalId;
        this.timeCreated = timeCreated;
        this.body = body;
        this.hash = hash;
    }

    @Override
    public boolean isNew() {
        return timeCreated == null;
    }

    public PullNotifDataPersistentState updateFromJson(JsonNode json, List<String> attributesSubset) {
        this.hash = calculateHash(json, attributesSubset);
        this.body = JsonUtils.writeObjectToJSONString(json);
        this.bodyJson = json;

        return this;
    }

    public static PullNotifDataPersistentState createFromJson(JsonNode json, String extIdAttributeName, List<String> attributesSubset) {
        String jsonString = JsonUtils.writeObjectToJSONString(json);
        String externalId = extractIdFromPayload(json, extIdAttributeName);

        PullNotifDataPersistentState notifData = PullNotifDataPersistentState.builder()
            .id(UUID.randomUUID())
            .externalId(externalId)
            .body(jsonString)
            .hash(calculateHash(json, attributesSubset))
            .build();
        notifData.setBodyJson(json);
        return notifData;
    }

    public static PullNotifDataPersistentState createFromJson(JsonNode json, String extIdAttributeName) {
        return createFromJson(json, extIdAttributeName, new ArrayList<>());
    }

    public static String extractIdFromPayload(JsonNode jsonNode, String externalIdAttrName) {
        JsonNode attr = jsonNode.get(externalIdAttrName);

        if (attr == null) {
            return null;
        }

        return attr.asText();
    }
        
    public static String calculateHash(JsonNode bodyJson, List<String> attributesSubset) {
        JsonNode bodyJsonAttributesSubset;
        if (attributesSubset == null || attributesSubset.isEmpty()) {
            bodyJsonAttributesSubset = bodyJson;
        } else {
            ObjectNode node = JsonUtils.createJsonObjectNode();

            List<String> nonExistingAttributesFromConfig = new ArrayList<>();
            for (String attribute : attributesSubset) {
                if (!bodyJson.has(attribute)) {
                    nonExistingAttributesFromConfig.add(attribute);
                }

                node.set(attribute, bodyJson.get(attribute));
            }
            if (!nonExistingAttributesFromConfig.isEmpty()) {
                throw new PayloadValidationException(String.format("Could not calculate hash, attributes [%s] not found in pulled data object",
                        nonExistingAttributesFromConfig.stream().collect(Collectors.joining(", "))));
            }

            bodyJsonAttributesSubset = node;
        }

        String jsonStr = JsonUtils.writeObjectToJSONString(bodyJsonAttributesSubset);
        return calculateHash(jsonStr);
    }

    public static String calculateHash(String jsonStr) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(sha256.digest(jsonStr.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 is not available", e);
        }
    }

}
