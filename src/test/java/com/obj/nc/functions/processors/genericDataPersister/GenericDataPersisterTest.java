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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

public class GenericDataPersisterTest {
    ObjectMapper mapper = new ObjectMapper();
    GenericDataRepository repo = Mockito.mock(GenericDataRepository.class);

    final String extId = "id-1";
    final String content = "{\"id\":\"" + extId + "\"}";
    final String emptyJson = "{}";

    @Test
    void testInvalidExternalIdAttrName() throws JsonProcessingException {
        JsonNode jsonNode = mapper.readTree(emptyJson);

        GenericDataPersister persister = new GenericDataPersister(repo, "id");
        PayloadValidationException thrown = Assertions.assertThrows(PayloadValidationException.class, () -> persister.apply(Collections.singletonList(jsonNode)));
        Assertions.assertEquals("One of polled items is missing attribute with name id. Set it in nc.data-sources.\"your-datasource\".externalId*", thrown.getMessage());

        Mockito.verify(repo, Mockito.never()).findAllHashesByExternalId(ArgumentMatchers.any());
        Mockito.verify(repo, Mockito.never()).saveAll(ArgumentMatchers.any());
    }

    @Test
    void testPersistingNew() throws JsonProcessingException {
        verifyPersistingProcess(Collections.emptyList());
    }

    @Test
    void testPersistingUpdated() throws JsonProcessingException {
        GenericData previous = GenericData.builder().externalId(extId).hash(HashFunction.hash(emptyJson)).build();
        verifyPersistingProcess(Collections.singletonList(previous));
    }

    private void verifyPersistingProcess(List<GenericData> storedRecords) throws JsonProcessingException {
        // given
        JsonNode jsonNode = mapper.readTree(content);
        Mockito.when(repo.findAllHashesByExternalId(Collections.singletonList(extId))).thenReturn(storedRecords);
        Mockito.when(repo.saveAll(ArgumentMatchers.any())).thenAnswer(a -> a.getArgument(0));

        // when
        GenericDataPersister persister = new GenericDataPersister(repo, "id");
        persister.apply(Collections.singletonList(jsonNode));

        // then
        Mockito.verify(repo).findAllHashesByExternalId(Collections.singletonList(extId));

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(repo).saveAll(captor.capture());

        List list = captor.getValue();
        Assertions.assertEquals(1, list.size());

        GenericData data = (GenericData) list.get(0);
        Assertions.assertEquals(extId, data.getExternalId());
        Assertions.assertEquals(content, data.getBody());
        Assertions.assertEquals(HashFunction.hash(content), data.getHash());
    }

}