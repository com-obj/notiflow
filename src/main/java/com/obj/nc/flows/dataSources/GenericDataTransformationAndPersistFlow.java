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

package com.obj.nc.flows.dataSources;

import com.obj.nc.functions.processors.genericDataPersister.PulledNotificationDataNewAndChangedPersister;
import com.obj.nc.functions.processors.jsonNodeToGenericDataTransformer.Data2PulledNotificationDataTransformer;
import com.obj.nc.functions.processors.jsonNodeToGenericDataTransformer.JsonNodeFilterAndTransformer;
import com.obj.nc.repositories.GenericDataRepository;
import com.obj.nc.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.StandardIntegrationFlow;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.json.ObjectToJsonTransformer;
import org.springframework.stereotype.Component;

import static com.obj.nc.flows.dataSources.GenericDataConvertingFlowConfiguration.GENERIC_DATA_CONVERTING_FLOW_ID_INPUT_CHANNEL_ID;

@Component
@RequiredArgsConstructor
public class GenericDataTransformationAndPersistFlow {
    private final GenericDataRepository genericDataRepository;

    public StandardIntegrationFlow continueFlow(IntegrationFlowBuilder builder, JobConfig jobConfig) {
        String externalIdKey = selectExternalIdKey(jobConfig);

        return builder.transform(Transformers.toJson(JsonUtils.getJsonObjectMapper(), ObjectToJsonTransformer.ResultType.NODE))
                .split() // split ArrayNode to JsonNode-s
                .aggregate() // aggregate JsonNode-s to List<JsonNode>
                .handle(new PulledNotificationDataNewAndChangedPersister(genericDataRepository, externalIdKey))
                .handle(new JsonNodeFilterAndTransformer(jobConfig.getPojoFCCN(), jobConfig.getSpelFilterExpression()))
                .handle(new Data2PulledNotificationDataTransformer())
                .channel(GENERIC_DATA_CONVERTING_FLOW_ID_INPUT_CHANNEL_ID)
                .get();
    }

    private String selectExternalIdKey(JobConfig jobConfig) {
        String idAttrName = jobConfig.getExternalIdAttrName();

        if (idAttrName == null) {
            idAttrName = "id";
        }
        return idAttrName;
    }
}
