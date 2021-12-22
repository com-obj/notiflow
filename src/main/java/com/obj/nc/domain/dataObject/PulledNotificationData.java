/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.domain.dataObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PulledNotificationData<PT> {
    
    // private Metadata metadata;
    private List<PT> payloads;

    public <T> List<T> getPayloadsAsPojo(Class<T> pojoClass) {
        return getPayloads()
                .stream()
                .map(node -> JsonUtils.readObjectFromJSON((JsonNode)node, pojoClass))
                .collect(Collectors.toList());
    }
        
}