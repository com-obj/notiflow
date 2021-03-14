package com.obj.nc.controllers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.assertj.core.api.Assertions;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.jayway.jsonpath.JsonPath;
import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersisterConsumer;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@AutoConfigureMockMvc
class EventReceiverTest extends BaseIntegrationTest {

//    @Autowired
//    private GenericEventPersisterConsumer genericEventPersister;
    
    @Autowired
    private GenericEventRepository genericEventRepository;

    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    void setUp() {
    }
    
    @Test
    void testPersistPIForEventWithRecipients() throws Exception {
        // given
        String INPUT_JSON_FILE = "events/generic_event.json";
        String eventJson = JsonUtils.readJsonStringFromClassPathResource(INPUT_JSON_FILE);
        
        //when
        ResultActions resp = mockMvc
        		.perform(MockMvcRequestBuilders.post("/events")
        		.contentType(APPLICATION_JSON_UTF8)
        		.content(eventJson)
                .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //then
        resp
        	.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.ncEventId").value(CoreMatchers.notNullValue()));
        
        String eventId = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.ncEventId");
        GenericEvent genericEvent = genericEventRepository.findById(eventId).get();
        
        Assertions.assertThat(genericEvent).isNotNull();
    }

 
}
