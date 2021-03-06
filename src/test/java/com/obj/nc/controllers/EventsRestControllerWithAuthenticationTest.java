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

import com.jayway.jsonpath.JsonPath;
import com.obj.nc.config.NcAppConfigProperties;
import com.obj.nc.security.config.JwtTokenUtil;
import com.obj.nc.security.config.NcJwtConfigProperties;
import com.obj.nc.security.exception.UserNotAuthenticatedException;
import com.obj.nc.security.model.JwtRequest;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.ArrayList;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static com.obj.nc.security.config.Constants.JWT_TOKEN_PREFIX;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
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
@DirtiesContext
class EventsRestControllerWithAuthenticationTest extends BaseIntegrationTest {
    
	@Autowired protected MockMvc mockMvc;
	@Autowired private JwtTokenUtil jwtTokenUtil;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private NcJwtConfigProperties ncJwtConfigProperties;
    @Autowired private NcAppConfigProperties ncAppConfigProperties;

    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
    	purgeNotifTables(jdbcTemplate);
    }
    
    @Test
    void testReceiveEventWithValidCredentials() throws Exception {
        // given
        String INPUT_JSON_FILE = "events/generic_event.json";
        String eventJson = JsonUtils.readJsonStringFromClassPathResource(INPUT_JSON_FILE);
    
        JwtRequest jwtRequest = JwtRequest.builder().username("testUser").password("testPassword").build();
        ResultActions authResponse = mockMvc
                .perform(MockMvcRequestBuilders
                        .post(ncAppConfigProperties.getContextPath() + "/authenticate")
                        .contextPath(ncAppConfigProperties.getContextPath())
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(jwtRequest.toString())
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        String token = JsonPath.read(authResponse.andReturn().getResponse().getContentAsString(), "$.token");
        
        // when
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/events")
                .contentType(APPLICATION_JSON_UTF8)
                .content(eventJson)
                .accept(APPLICATION_JSON_UTF8)
                .header(AUTHORIZATION, "Bearer ".concat(token)))
                .andDo(MockMvcResultHandlers.print());
        
        // then
        response.andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.ncEventId").value(CoreMatchers.notNullValue()));
        
    }
    
    @Test
    void testReceiveEventWithNoToken() {
        // given
        String INPUT_JSON_FILE = "events/generic_event.json";
        String eventJson = JsonUtils.readJsonStringFromClassPathResource(INPUT_JSON_FILE);
        
        //when
        assertThatThrownBy(() -> mockMvc.perform(MockMvcRequestBuilders.post("/events")
                    .contentType(APPLICATION_JSON_UTF8)
                    .content(eventJson)
                    .accept(APPLICATION_JSON_UTF8)))
                .isInstanceOf(UserNotAuthenticatedException.class)
                .hasMessageContaining("JWT Token does not begin with Bearer String");
    }
    
    @Test
    void testReceiveEventWithInvalidSignature() {
        // given
        String INPUT_JSON_FILE = "events/generic_event.json";
        String eventJson = JsonUtils.readJsonStringFromClassPathResource(INPUT_JSON_FILE);
        String token = jwtTokenUtil.generateToken(new User("testUser", passwordEncoder.encode("testPassword"), new ArrayList<>()), 
                "invalidSignature");
        
        //when
        assertThatThrownBy(() -> mockMvc.perform(MockMvcRequestBuilders.post("/events")
                    .contentType(APPLICATION_JSON_UTF8)
                    .content(eventJson)
                    .accept(APPLICATION_JSON_UTF8)
                    .header(AUTHORIZATION, JWT_TOKEN_PREFIX.concat(token))))
                .isInstanceOf(UserNotAuthenticatedException.class)
                .hasMessageContaining("JWT signature does not match locally computed signature.");
    }
    
    @Test
    void testReceiveEventWithInvalidUsername() {
        // given
        String INPUT_JSON_FILE = "events/generic_event.json";
        String eventJson = JsonUtils.readJsonStringFromClassPathResource(INPUT_JSON_FILE);
        String token = jwtTokenUtil.generateToken(new User("invalidUser", passwordEncoder.encode("testPassword"), new ArrayList<>()), 
                ncJwtConfigProperties.getSignatureSecret());
        
        //when
        assertThatThrownBy(() -> mockMvc
                .perform(MockMvcRequestBuilders.post("/events")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(eventJson)
                        .accept(APPLICATION_JSON_UTF8)
                        .header(AUTHORIZATION, JWT_TOKEN_PREFIX.concat(token))))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with username: invalidUser");
    }
 
}
