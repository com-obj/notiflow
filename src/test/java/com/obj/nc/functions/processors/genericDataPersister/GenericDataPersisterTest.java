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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.domain.genericData.GenericData;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.repositories.GenericDataRepository;
import com.obj.nc.utils.JsonUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GenericDataPersisterTest {
    ObjectMapper mapper = new ObjectMapper();
    GenericDataRepository repo = Mockito.mock(GenericDataRepository.class);

    final String extId1 = "id-1";
    final String extId2 = "id-2";
    final String content1 = "{\"id\" : \"" + extId1 + "\" , \"data\" : 3}";
    final String content1_changed = "{ \"id\": \"" + extId1 + "\" , \"data\" : 3}";
    final String content2 = "{\"id\":\"" + extId2 + "\",\"data\": 3}";
    final String content_other = "{\"id\" :\"other\" , \"data\": 3}";
    final String pulledData = "["+content1+","+content2+"]";
    final String invalidJson = "[{\"id-other-name\":\"" + extId1 + "\"}]";

    @Test
    void testInvalidExternalIdAttrName() throws JsonProcessingException {
        List<JsonNode> pulledNotifData = JsonUtils.readJsonNodeListFromJSONString(invalidJson);

        PulledNotificationDataNewAndChangedPersister persister = new PulledNotificationDataNewAndChangedPersister(repo, "id");

        PayloadValidationException thrown = Assertions.assertThrows(
            PayloadValidationException.class, () -> persister.apply(pulledNotifData));
        Assertions.assertEquals("One of polled items is missing attribute with name id. Set it in nc.data-sources.\"your-datasource\".externalId*", thrown.getMessage());

        Mockito.verify(repo, Mockito.never()).findAllHashesByExternalId(ArgumentMatchers.any());
        Mockito.verify(repo, Mockito.never()).saveAll(ArgumentMatchers.any());
    }

    @Test
    void testPersistingNew() throws JsonProcessingException {
        List<JsonNode> pulledNotifData = JsonUtils.readJsonNodeListFromJSONString(pulledData);

        GenericData expectPersisted1 = GenericData.createFromJson(JsonUtils.readJsonNodeFromJSONString(content1), "id");
        GenericData expectPersisted2 = GenericData.createFromJson(JsonUtils.readJsonNodeFromJSONString(content2), "id");

        List<GenericData> existingData = Collections.emptyList();

        verifyPersistingProcess(existingData, pulledNotifData, expectPersisted1, expectPersisted2);
    }

    @Test
    void testPersistingNew2() throws JsonProcessingException {
        List<JsonNode> pulledNotifData = JsonUtils.readJsonNodeListFromJSONString(pulledData);

        GenericData expectPersisted1 = GenericData.createFromJson(JsonUtils.readJsonNodeFromJSONString(content1), "id");
        GenericData expectPersisted2 = GenericData.createFromJson(JsonUtils.readJsonNodeFromJSONString(content2), "id");

        List<GenericData> existingData = Collections.singletonList(GenericData.createFromJson(JsonUtils.readJsonNodeFromJSONString(content_other), "id"));

        verifyPersistingProcess(existingData, pulledNotifData, expectPersisted1, expectPersisted2);
    }

    @Test
    void testPersistingNewAndChanged() throws JsonProcessingException {
        List<JsonNode> pulledNotifData = JsonUtils.readJsonNodeListFromJSONString(pulledData);

        GenericData expectPersisted1 = GenericData.createFromJson(JsonUtils.readJsonNodeFromJSONString(content1), "id");
        GenericData expectPersisted2 = GenericData.createFromJson(JsonUtils.readJsonNodeFromJSONString(content2), "id");

        GenericData previous = GenericData.createFromJson(JsonUtils.readJsonNodeFromJSONString(content_other), "id");           

        verifyPersistingProcess(Collections.singletonList(previous),pulledNotifData, expectPersisted1, expectPersisted2);
    }

    @Test
    void testNotPersistingExisting() throws JsonProcessingException {
        List<JsonNode> pulledNotifData = JsonUtils.readJsonNodeListFromJSONString(pulledData);

        GenericData expectPersisted2 = GenericData.createFromJson(JsonUtils.readJsonNodeFromJSONString(content2), "id");

        GenericData previous = GenericData.createFromJson(JsonUtils.readJsonNodeFromJSONString(content1), "id");           

        verifyPersistingProcess(Collections.singletonList(previous),pulledNotifData, expectPersisted2);
    }

    private void verifyPersistingProcess(List<GenericData> storedRecords, List<JsonNode> pulledNotifData, GenericData ... expectedPersisted) throws JsonProcessingException {
        // given
        Mockito.when(repo.findAllHashesByExternalId(Arrays.asList(extId1, extId2))).thenReturn(storedRecords);
        Mockito.when(repo.saveAll(ArgumentMatchers.any())).thenAnswer(a -> a.getArgument(0));

        // when
        PulledNotificationDataNewAndChangedPersister persister = new PulledNotificationDataNewAndChangedPersister(repo, "id");
        List<JsonNode> notifDataNewAndChanged = persister.apply(pulledNotifData);

        // then
        //only new and changed get processed further
        Assertions.assertEquals(expectedPersisted.length, notifDataNewAndChanged.size());

        //all input notifs have been checked
        Mockito.verify(repo).findAllHashesByExternalId(Arrays.asList(extId1, extId2));

        //all new and changed have been persisted
        ArgumentCaptor<List<GenericData>> newAndChangedCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(repo).saveAll(newAndChangedCaptor.capture());

        List<GenericData> newAndChanged = newAndChangedCaptor.getValue();
        Assertions.assertEquals(expectedPersisted.length, newAndChanged.size());

        for (int i =0; i< expectedPersisted.length; i++) {
            assertEquals(expectedPersisted[i], newAndChanged.get(i));
        }
    }

    private void assertEquals(GenericData expected, GenericData actual) {
        Assertions.assertEquals(expected.getExternalId(), actual.getExternalId());
        Assertions.assertEquals(expected.getBody(), actual.getBody());
        Assertions.assertEquals(expected.getHash(), actual.getHash());
    }

}
