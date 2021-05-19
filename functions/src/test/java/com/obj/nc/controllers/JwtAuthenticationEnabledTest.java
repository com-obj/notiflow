package com.obj.nc.controllers;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.security.model.JwtRequest;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "nc.jwt.enabled=true",
        "nc.jwt.username=testUser",
        "nc.jwt.password=testPassword",
        "nc.jwt.signature-secret=testSecret"
})
@DirtiesContext
class JwtAuthenticationEnabledTest extends BaseIntegrationTest {
    
	@Autowired protected MockMvc mockMvc;
    
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
        		.perform(MockMvcRequestBuilders.post("/authenticate")
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
                .perform(MockMvcRequestBuilders.post("/authenticate")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(jwtRequest.toString())
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
    
        //then
        resp.andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value(CoreMatchers.containsString("Bad credentials")));
        
    }
 
}
