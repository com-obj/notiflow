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

package com.obj.nc.controllers;

import com.obj.nc.config.NcAppConfigProperties;
import com.obj.nc.security.model.JwtRequest;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@AutoConfigureMockMvc
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest(properties = {
        "nc.jwt.enabled=true",
        "nc.jwt.username=testUser",
        "nc.jwt.password=testPassword",
        "nc.jwt.signature-secret=testSecret"
})
class JwtAuthenticationEnabledTest extends BaseIntegrationTest {
    
	@Autowired protected MockMvc mockMvc;
	@Autowired private NcAppConfigProperties ncAppConfigProperties;
    
    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
        purgeNotifTables(jdbcTemplate);
    }
    
    @Test
    void testAuthenticateSucces() throws Exception {
        // given
        JwtRequest jwtRequest = JwtRequest.builder().username("testUser").password("testPassword").build();
        
        //when
        ResultActions resp = mockMvc
        		.perform(MockMvcRequestBuilders
                        .post(ncAppConfigProperties.getContextPath() + "/authenticate")
                        .contextPath(ncAppConfigProperties.getContextPath())
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(jwtRequest.toString())
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());

        //then
        resp.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.token").value(CoreMatchers.notNullValue()));
    }
    
    @Test
    void testAuthenticateBadUsername() throws Exception {
        // given
        JwtRequest jwtRequest = JwtRequest.builder().username("badTestUser").password("testPassword").build();
    
        //when
        ResultActions resp = mockMvc
                .perform(MockMvcRequestBuilders
                        .post(ncAppConfigProperties.getContextPath() + "/authenticate")
                        .contextPath(ncAppConfigProperties.getContextPath())
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(jwtRequest.toString())
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
    
        //then
        resp.andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value(CoreMatchers.containsString("Bad credentials")));
        
    }
 
}
