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

package com.obj.nc.flows.dataSources.http;

public class HttpDatasourceNameCreator {

    public static final String PULL_HTTP_DS_POLLER_POSTFIX = "_POLLER";
    public static final String PULL_HTTP_DS_NAME_PREFIX = "NC_HTTP_DATA_SOURCE_";
    public static final String PULL_HTTP_DS_JOB_POSTFIX = "_INTEGRATION_FLOW";

    private HttpDatasourceNameCreator() {
    }

    static String createDataSourceId(String dataSourceName) {
        return PULL_HTTP_DS_NAME_PREFIX.concat(dataSourceName);
    }

    static String createJobFlowId(String dataSourceName) {
        return createDataSourceId(dataSourceName).concat(PULL_HTTP_DS_JOB_POSTFIX);
    }

    static String createJobPollerId(String dataSourceName) {
        return createJobFlowId(dataSourceName).concat(PULL_HTTP_DS_POLLER_POSTFIX);
    }
}
