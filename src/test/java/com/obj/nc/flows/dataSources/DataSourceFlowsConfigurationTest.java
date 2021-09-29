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

package com.obj.nc.flows.dataSources;

import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

@Disabled
@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = {
        "NC_CUSTOM_DATA_SOURCE_first-jdbc.payload-regular_INTEGRATION_FLOW_POLLER"
})
@SpringBootTest(properties = {
        "nc.data-sources.jdbc[0].name=first-jdbc",
        "nc.data-sources.jdbc[0].url=jdbc:postgresql://localhost:25432/nc",
        "nc.data-sources.jdbc[0].username=nc",
        "nc.data-sources.jdbc[0].password=ZMss4o9mdBLV",
        "nc.data-sources.jdbc[0].jobs[0].name=payload-regular",
        "nc.data-sources.jdbc[0].jobs[0].description=Vsetky payloady",
        "nc.data-sources.jdbc[0].jobs[0].template-path=src/test/resources/templates/payload-expiry.html",
        "nc.data-sources.jdbc[0].jobs[0].table-name=test_payload",
        "nc.data-sources.jdbc[0].jobs[0].cron=*/3 * * * * *"
})
public class DataSourceFlowsConfigurationTest extends BaseIntegrationTest {
    // TODO : spravit/nespravit??
}
