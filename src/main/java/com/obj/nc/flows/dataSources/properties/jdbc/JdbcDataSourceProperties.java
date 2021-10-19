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
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.UniqueElements;

import javax.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(of = "name")
@ToString(of = "name")
public class JdbcDataSourceProperties {
    @NotEmpty
    private String name;
    @NotEmpty
    private String url;
    @NotEmpty
    private String username;
    @NotEmpty
    private String password;

    @UniqueElements
    private List<JdbcJobProperties> jobs = new ArrayList<>();
}
