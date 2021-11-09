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

package com.obj.nc.flows.dataSources.properties;

import com.obj.nc.flows.dataSources.properties.http.HttpDataSourceProperties;
import com.obj.nc.flows.dataSources.properties.jdbc.JdbcDataSourceProperties;
import lombok.Data;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Data
@Validated
@Configuration
@ConfigurationProperties("nc.data-sources")
public class DataSourceFlowsProperties {
    @UniqueElements
    private List<JdbcDataSourceProperties> jdbc = new ArrayList<>();

    @UniqueElements
    private List<HttpDataSourceProperties> http = new ArrayList<>();
}
