package com.obj.nc.koderia.integration;

import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
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
@SpringBootTest(properties = "nc.jwt.enabled=false")
@DirtiesContext
class EventReceiverTest extends BaseIntegrationTest {
    
	@Autowired protected MockMvc mockMvc;

    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
    	purgeNotifTables(jdbcTemplate);
    }
    
    @Test
    void testReceiveValidJobPostEvent() throws Exception {
        // given
        String validJobPostEvent = JsonUtils.readJsonStringFromClassPathResource("koderia/create_request/job_body.json");
        
        //when
        ResultActions resp = mockMvc
                .perform(MockMvcRequestBuilders.post("/events")
                        .param("payloadType", "JOB_POST")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(validJobPostEvent)
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //then
        resp.andExpect(status().is2xxSuccessful());
    }
    
    @Test
    void testReceiveJobPostEventWithoutDescription() throws Exception {
        // given
        String jobPostEventWithoutDescription = JsonUtils.readJsonStringFromClassPathResource("koderia/create_request/job_body_no_text.json");
    
        //when
        ResultActions resp = mockMvc
        		.perform(MockMvcRequestBuilders.post("/events")
                        .param("payloadType", "JOB_POST")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(jobPostEventWithoutDescription)
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //then
        resp
        	.andExpect(status().is4xxClientError())
			.andExpect(jsonPath("$").value(CoreMatchers.startsWith("Request not valid becase of invalid payload: Payload")));
        
    }
 
}
