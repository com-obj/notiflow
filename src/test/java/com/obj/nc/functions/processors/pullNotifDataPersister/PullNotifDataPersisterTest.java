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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.domain.pullNotifData.PullNotifDataPersistentState;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.repositories.PullNotifDataRepository;
import com.obj.nc.utils.JsonUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PullNotifDataPersisterTest {
    ObjectMapper mapper = new ObjectMapper();
    PullNotifDataRepository repo = Mockito.mock(PullNotifDataRepository.class);

    final String extId1 = "id-1";
    final String extId2 = "id-2";
    final List<String> extIds12 = Arrays.asList(extId1, extId2);
    final String extId3 = "id-3";
    final String content1 = "{\"id\" : \"" + extId1 + "\" , \"data\" : 3}";
    final String content1_changed = "{ \"id\": \"" + extId1 + "\" , \"data\" : 3}";
    final String content2 = "{\"id\":\"" + extId2 + "\",\"data\": 3}";
    final String content3 = "{\"id\":\"" + extId3 + "\",\"data\": 3}";
    final String content4 = "{\"id\":\"" + extId3 + "\",\"data\": 4}";
    final String content_other = "{\"id\" :\"other\" , \"data\": 3}";
    final String pulledData = "["+content1+","+content2+"]";
    final String pulledData2 = "["+content4+"]";
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

        PullNotifDataPersistentState expectPersisted1 = PullNotifDataPersistentState.createFromJson(JsonUtils.readJsonNodeFromJSONString(content1), "id");
        PullNotifDataPersistentState expectPersisted2 = PullNotifDataPersistentState.createFromJson(JsonUtils.readJsonNodeFromJSONString(content2), "id");

        List<PullNotifDataPersistentState> existingData = Collections.emptyList();

        verifyPersistingProcess(extIds12, existingData, pulledNotifData, expectPersisted1, expectPersisted2);
    }

    @Test
    void testPersistingNew2() throws JsonProcessingException {
        List<JsonNode> pulledNotifData = JsonUtils.readJsonNodeListFromJSONString(pulledData);

        PullNotifDataPersistentState expectPersisted1 = PullNotifDataPersistentState.createFromJson(JsonUtils.readJsonNodeFromJSONString(content1), "id");
        PullNotifDataPersistentState expectPersisted2 = PullNotifDataPersistentState.createFromJson(JsonUtils.readJsonNodeFromJSONString(content2), "id");

        List<PullNotifDataPersistentState> existingData = Collections.singletonList(PullNotifDataPersistentState.createFromJson(JsonUtils.readJsonNodeFromJSONString(content_other), "id"));

        verifyPersistingProcess(extIds12, existingData, pulledNotifData, expectPersisted1, expectPersisted2);
    }

    @Test
    void testPersistingNewAndChanged() throws JsonProcessingException {
        List<JsonNode> pulledNotifData = JsonUtils.readJsonNodeListFromJSONString(pulledData);

        PullNotifDataPersistentState expectPersisted1 = PullNotifDataPersistentState.createFromJson(JsonUtils.readJsonNodeFromJSONString(content1), "id");
        PullNotifDataPersistentState expectPersisted2 = PullNotifDataPersistentState.createFromJson(JsonUtils.readJsonNodeFromJSONString(content2), "id");

        PullNotifDataPersistentState previous = PullNotifDataPersistentState.createFromJson(JsonUtils.readJsonNodeFromJSONString(content_other), "id");

        verifyPersistingProcess(extIds12, Collections.singletonList(previous),pulledNotifData, expectPersisted1, expectPersisted2);
    }

    @Test
    void testNotPersistingExisting() throws JsonProcessingException {
        List<JsonNode> pulledNotifData = JsonUtils.readJsonNodeListFromJSONString(pulledData);

        PullNotifDataPersistentState expectPersisted2 = PullNotifDataPersistentState.createFromJson(JsonUtils.readJsonNodeFromJSONString(content2), "id");

        PullNotifDataPersistentState previous = PullNotifDataPersistentState.createFromJson(JsonUtils.readJsonNodeFromJSONString(content1), "id");

        verifyPersistingProcess(extIds12, Collections.singletonList(previous),pulledNotifData, expectPersisted2);
    }

    @Test
    void testPersistingExistingWithAttributesSubset() throws JsonProcessingException {
        List<JsonNode> pulledNotifData = JsonUtils.readJsonNodeListFromJSONString(pulledData2);
        // data field will be changed
        List<String> hashAttributes = Arrays.asList("data");

        PullNotifDataPersistentState expectPersisted = PullNotifDataPersistentState.createFromJson(JsonUtils.readJsonNodeFromJSONString(content4), "id", hashAttributes);
        PullNotifDataPersistentState previous = PullNotifDataPersistentState.createFromJson(JsonUtils.readJsonNodeFromJSONString(content3), "id", hashAttributes);

        verifyPersistingProcess(Arrays.asList(extId3), Collections.singletonList(previous), pulledNotifData, hashAttributes, expectPersisted);
    }

    @Test
    void testNotPersistingExistingWithAttributesSubset() throws JsonProcessingException {
        List<JsonNode> pulledNotifData = JsonUtils.readJsonNodeListFromJSONString(pulledData2);
        // id field will not be changed
        List<String> hashAttributes = Arrays.asList("id");

        PullNotifDataPersistentState previous = PullNotifDataPersistentState.createFromJson(JsonUtils.readJsonNodeFromJSONString(content3), "id", hashAttributes);

        verifyPersistingProcess(Arrays.asList(extId3), Collections.singletonList(previous), pulledNotifData, hashAttributes);
    }

    private void verifyPersistingProcess(List<String> extIds, List<PullNotifDataPersistentState> storedRecords, List<JsonNode> pulledNotifData, List<String> hashAttributes, PullNotifDataPersistentState ... expectedPersisted) throws JsonProcessingException {
        // given
        Mockito.when(repo.findAllHashesByExternalId(extIds)).thenReturn(storedRecords);
        Mockito.when(repo.saveAll(ArgumentMatchers.any())).thenAnswer(a -> a.getArgument(0));

        // when
        PulledNotificationDataNewAndChangedPersister persister = new PulledNotificationDataNewAndChangedPersister(repo, "id", hashAttributes);
        List<JsonNode> notifDataNewAndChanged = persister.apply(pulledNotifData);

        // then
        //only new and changed get processed further
        Assertions.assertEquals(expectedPersisted.length, notifDataNewAndChanged.size());

        //all input notifs have been checked
        Mockito.verify(repo).findAllHashesByExternalId(extIds);

        //all new and changed have been persisted
        ArgumentCaptor<List<PullNotifDataPersistentState>> newAndChangedCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(repo).saveAll(newAndChangedCaptor.capture());

        List<PullNotifDataPersistentState> newAndChanged = newAndChangedCaptor.getValue();
        Assertions.assertEquals(expectedPersisted.length, newAndChanged.size());

        for (int i =0; i< expectedPersisted.length; i++) {
            assertEquals(expectedPersisted[i], newAndChanged.get(i));
        }
    }

    private void verifyPersistingProcess(List<String> extIds, List<PullNotifDataPersistentState> storedRecords, List<JsonNode> pulledNotifData, PullNotifDataPersistentState ... expectedPersisted) throws JsonProcessingException {
        verifyPersistingProcess(extIds, storedRecords, pulledNotifData, new ArrayList<>(), expectedPersisted);
    }

    private void assertEquals(PullNotifDataPersistentState expected, PullNotifDataPersistentState actual) {
        Assertions.assertEquals(expected.getExternalId(), actual.getExternalId());
        Assertions.assertEquals(expected.getBody(), actual.getBody());
        Assertions.assertEquals(expected.getHash(), actual.getHash());
    }

}
