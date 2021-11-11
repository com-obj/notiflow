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

package com.obj.nc.flows.dataSources.properties.jdbc;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class JdbcJobProperties {
    @NotEmpty
    private String name;
    @NotEmpty
    private String sqlQuery;

    private String pojoFCCN; //if set, GenericData will be GenericDataPojo<FCCN>


    private String spelFilterExpression; 

    @NotEmpty
    private String cron;

    private String externalIdColumnName;
}
