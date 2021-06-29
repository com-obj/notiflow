package com.obj.nc.controllers;

import com.jayway.jsonpath.JsonPath;
import com.obj.nc.domain.message.MessagePersistantState;
import com.obj.nc.repositories.MessageRepository;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
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

import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@AutoConfigureMockMvc
@SpringBootTest
@DirtiesContext
class MessageReceiverTest extends BaseIntegrationTest {
    
    
	@Autowired private MessageRepository messageRepo;
	@Autowired protected MockMvc mockMvc;

    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
    	purgeNotifTables(jdbcTemplate);
    }
    
    @Test
    void testPersistMessage() throws Exception {
        // given
        String INPUT_JSON_FILE = "messages/email/email_message.json";
        String messageJson = JsonUtils.readJsonStringFromClassPathResource(INPUT_JSON_FILE);
        
        //when
        ResultActions resp = mockMvc
        		.perform(MockMvcRequestBuilders.post("/messages")
        		.contentType(APPLICATION_JSON_UTF8)
        		.content(messageJson)
                .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //then
        resp
        	.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.ncMessageId").value(CoreMatchers.notNullValue()));
        
        String messageId = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.ncMessageId");
        
        Optional<MessagePersistantState> messagePS = messageRepo.findById(UUID.fromString(messageId));
        Assertions.assertThat(messagePS.isPresent()).isTrue();
    }
    
    @Test
    void testDuplicatePersistWithExternalId() throws Exception {
        // given
        String INPUT_JSON_FILE = "messages/email/email_message_with_external_id.json";
        String messageJson = JsonUtils.readJsonStringFromClassPathResource(INPUT_JSON_FILE);
        
        //when
        ResultActions resp = mockMvc
        		.perform(MockMvcRequestBuilders.post("/messages")
        		.contentType(APPLICATION_JSON_UTF8)
        		.content(messageJson)
                .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //then
        resp
        	.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.ncMessageId").value(CoreMatchers.notNullValue()));
        
        //and when
        resp = mockMvc
        		.perform(MockMvcRequestBuilders.post("/messages")
        		.contentType(APPLICATION_JSON_UTF8)
        		.content(messageJson)
                .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //then
        resp
        	.andExpect(status().is4xxClientError())
			.andExpect(jsonPath("$").value(CoreMatchers.startsWith("Request not valid becase of invalid payload: Duplicate external ID detected")));

    }
    
    @Test
    void testOverideExternalAndFlowId() throws Exception {
        // given
        String INPUT_JSON_FILE = "messages/email/email_message_with_external_id.json";
        String messageJson = JsonUtils.readJsonStringFromClassPathResource(INPUT_JSON_FILE);
        
        //when
        ResultActions resp = mockMvc
        		.perform(MockMvcRequestBuilders.post("/messages")
        		.param("flowId", "FLOW_ID_OVERRIDE")
        		.param("externalId", "EXTERNAL_ID_OVERRIDE")
        		.contentType(APPLICATION_JSON_UTF8)
        		.content(messageJson)
                .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //then
        resp
        	.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.ncMessageId").value(CoreMatchers.notNullValue()));
        
        String eventId = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.ncMessageId");
        MessagePersistantState messagePS = messageRepo.findById(UUID.fromString(eventId)).get();
        
        Assertions.assertThat(messagePS.getExternalId()).isEqualTo("EXTERNAL_ID_OVERRIDE");
        Assertions.assertThat(messagePS.getHeader().getFlowId()).isEqualTo("FLOW_ID_OVERRIDE");
    }
    
    @Test
    void testPersistPIForMessageWithFlowId() throws Exception {
        // given
        String INPUT_JSON_FILE = "messages/email/email_message_with_flow_id.json";
        String eventJson = JsonUtils.readJsonStringFromClassPathResource(INPUT_JSON_FILE);
        
        //when
        ResultActions resp = mockMvc
        		.perform(MockMvcRequestBuilders.post("/messages")
        		.contentType(APPLICATION_JSON_UTF8)
        		.content(eventJson)
                .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //then
        resp
        	.andExpect(status().is2xxSuccessful());
        
        String eventId = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.ncMessageId");
        MessagePersistantState messagePS = messageRepo.findById(UUID.fromString(eventId)).get();

        Assertions.assertThat(messagePS.getHeader().getFlowId()).isEqualTo("FLOW_ID");
    }
    
    @Test
    void testPersistPIForMessageWithExternalId() throws Exception {
        // given
        String INPUT_JSON_FILE = "messages/email/email_message_with_external_id.json";
        String eventJson = JsonUtils.readJsonStringFromClassPathResource(INPUT_JSON_FILE);
        
        //when
        ResultActions resp = mockMvc
        		.perform(MockMvcRequestBuilders.post("/messages")
        		.contentType(APPLICATION_JSON_UTF8)
        		.content(eventJson)
                .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //then
        resp
        	.andExpect(status().is2xxSuccessful());
        
        String eventId = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.ncMessageId");
        MessagePersistantState messagePS = messageRepo.findById(UUID.fromString(eventId)).get();

        Assertions.assertThat(messagePS.getExternalId()).isEqualTo("EXTERNAL_ID");
    }
    
    @Test
    void testPersistPIForNullRequest() throws Exception {        
        //when
        ResultActions resp = mockMvc
        		.perform(MockMvcRequestBuilders.post("/messages")
        		.contentType(APPLICATION_JSON_UTF8)
        		.content((byte[])null)
                .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //then
        resp
        	.andExpect(status().is4xxClientError())
			.andExpect(jsonPath("$").value(CoreMatchers.startsWith("Request arguments not valid: Required request body is missing")));
        
    }
 
}
