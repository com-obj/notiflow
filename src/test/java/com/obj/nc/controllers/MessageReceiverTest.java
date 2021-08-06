package com.obj.nc.controllers;

import com.jayway.jsonpath.JsonPath;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.MessagePersistantState;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.repositories.EndpointsRepository;
import com.obj.nc.repositories.MessageRepository;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@AutoConfigureMockMvc
@SpringBootTest
@DirtiesContext
class MessageReceiverTest extends BaseIntegrationTest {
    @Autowired private MessageRepository messageRepository;
	@Autowired private MockMvc mockMvc;

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
        		.perform(MockMvcRequestBuilders.post("/messages/send-email")
        		.contentType(APPLICATION_JSON_UTF8)
        		.content(messageJson)
                .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //then
        resp
        	.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.ncMessageId").value(CoreMatchers.notNullValue()));
        
        String messageId = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.ncMessageId");
        Assertions.assertThat(messageId).isNotNull();
    
        //and then
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> {
            List<MessagePersistantState> messages = StreamSupport
                    .stream(messageRepository.findAll().spliterator(), false)
                    .collect(Collectors.toList());
            return messages.size() >= 2;
        });
    
        List<MessagePersistantState> messages = StreamSupport
                .stream(messageRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
        
        Assertions.assertThat(messages).hasSize(2);
        
        Assertions.assertThat(messages.get(0).getBody()).isInstanceOf(EmailContent.class);
        Assertions.assertThat(((EmailContent) messages.get(0).getBody()).getContentType()).isEqualTo(MediaType.TEXT_HTML_VALUE);
        
        Assertions.assertThat(messages.get(1).getBody()).isInstanceOf(EmailContent.class);
        Assertions.assertThat(((EmailContent) messages.get(1).getBody()).getContentType()).isEqualTo(MediaType.TEXT_HTML_VALUE);
    }
    
    @Test
    void testPersistPIForNullRequest() throws Exception {        
        //when
        ResultActions resp = mockMvc
        		.perform(MockMvcRequestBuilders.post("/messages/send-email")
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
