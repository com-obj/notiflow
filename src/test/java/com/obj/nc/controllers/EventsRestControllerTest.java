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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import com.obj.nc.Get;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import lombok.Builder;
import lombok.Data;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "docs/api/generated/event")
@Import(RestDocsConfiguration.class)
class EventsRestControllerTest extends BaseIntegrationTest {
        
        @Autowired private GenericEventRepository genericEventRepository;
        @Autowired private TestDataRestController testDataController;
	@Autowired protected MockMvc mockMvc;
	
    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {    	
    	purgeNotifTables(jdbcTemplate);
    }
    
    @Test
    void testPersistGenericEvent() throws Exception {
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
        GenericEvent genericEvent = genericEventRepository.findById(UUID.fromString(eventId)).get();
        
        assertThat(genericEvent).isNotNull();
    }
    
    @Test
    void docPersistGenericEvent() throws Exception {
    	 // given
        TestPayloadForDocs testPayloadForDocs = TestPayloadForDocs.builder()
                .attribute1("Your payload value1. Can be anything")
                .attribute2("Your payload value2. Can be anything")
                .build();
        
        //when
        mockMvc
                .perform(MockMvcRequestBuilders.post("/events")
                .contentType(APPLICATION_JSON_UTF8)
                .content(JsonUtils.writeObjectToJSONString(testPayloadForDocs))
                .accept(APPLICATION_JSON_UTF8))
                .andDo(
                        document("POST-events",
                                requestFields(testPayloadFields),
                                responseFields( 
                                        fieldWithPath("ncEventId").description("Internal notiflow ID assigned to the event. Can be used for searching")
                                )
                        )
                );
    }

    FieldDescriptor[] testPayloadFields = new FieldDescriptor[] {
        fieldWithPath("@class").description("Payload type information as created by @JsonTypeInfo(use = Id.CLASS). Used for payload de-serialization to POJO"),
        fieldWithPath("attribute1").description("JSON attribute as an example. Can be anything"),
        fieldWithPath("attribute2").description("JSON attribute as an example. Can be anything")        
    };

    
    @Test
    void testDuplicatePersistWithExternalId() throws Exception {
        // given
        String INPUT_JSON_FILE = "events/generic_event_with_external_id.json";
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
        
        //and when
        resp = mockMvc
        		.perform(MockMvcRequestBuilders.post("/events")
        		.contentType(APPLICATION_JSON_UTF8)
        		.content(eventJson)
                .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //then
        resp
        	.andExpect(status().is4xxClientError())
			.andExpect(jsonPath("$").value(CoreMatchers.startsWith("Request not valid because of invalid payload: Duplicate external ID detected")));

    }
    
    @Test
    void testOverrideExternalAndFlowId() throws Exception {
        // given
        String INPUT_JSON_FILE = "events/generic_event_with_external_and_flow_id.json";
        String eventJson = JsonUtils.readJsonStringFromClassPathResource(INPUT_JSON_FILE);
        
        //when
        ResultActions resp = mockMvc
        		.perform(MockMvcRequestBuilders.post("/events")
        		.param("flowId", "FLOW_ID_OVERRIDE")
        		.param("externalId", "EXTERNAL_ID_OVERRIDE")
        		.contentType(APPLICATION_JSON_UTF8)
        		.content(eventJson)
                .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //then
        resp
        	.andExpect(status().is2xxSuccessful())
			.andExpect(jsonPath("$.ncEventId").value(CoreMatchers.notNullValue()));
        
        String eventId = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.ncEventId");
        GenericEvent genericEvent = genericEventRepository.findById(UUID.fromString(eventId)).get();
        
        assertThat(genericEvent.getExternalId()).isEqualTo("EXTERNAL_ID_OVERRIDE");
        assertThat(genericEvent.getFlowId()).isEqualTo("FLOW_ID_OVERRIDE");
    }
    
    @Test
    void testPersistPIForEventWithFlowId() throws Exception {
        // given
        String INPUT_JSON_FILE = "events/generic_event_with_flow_id.json";
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
        	.andExpect(status().is2xxSuccessful());
        
        String eventId = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.ncEventId");
        GenericEvent genericEvent = genericEventRepository.findById(UUID.fromString(eventId)).get();
        
        assertThat(genericEvent.getFlowId()).isEqualTo("FLOW_ID");
    }
    
    @Test
    void testPersistPIForEventWithExternalId() throws Exception {
        // given
        String INPUT_JSON_FILE = "events/generic_event_with_external_id.json";
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
        	.andExpect(status().is2xxSuccessful());
        
        String eventId = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.ncEventId");
        GenericEvent genericEvent = genericEventRepository.findById(UUID.fromString(eventId)).get();
        
        assertThat(genericEvent.getExternalId()).isEqualTo("EXTERNAL_ID");
    }
    
    @Test
    void testPersistPIForNonParsableJson() throws Exception {
        // given
        String INPUT_JSON_FILE = "events/generic_event_non_parsable.json";
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
        	.andExpect(status().is4xxClientError())
			.andExpect(jsonPath("$").value(CoreMatchers.startsWith("Request not valid because of invalid payload: Unexpected character")));
        
    }
    
    @Test
    void testPersistPIForNullRequest() throws Exception {        
        //when
        ResultActions resp = mockMvc
        		.perform(MockMvcRequestBuilders.post("/events")
        		.contentType(APPLICATION_JSON_UTF8)
        		.content((byte[])null)
                .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //then
        resp
        	.andExpect(status().is4xxClientError())
			.andExpect(jsonPath("$").value(CoreMatchers.startsWith("Request arguments not valid: Required request body is missing")));
        
    }
    
    @Test
    void testJsonSchemaPresentValidEvent() throws Exception {
        // given
        String validJobPostEvent = JsonUtils.readJsonStringFromClassPathResource("custom_events/job_body.json");
        
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
    void testJsonSchemaPresentInvalidEvent() throws Exception {
        // given
        String jobPostEventWithoutDescription = JsonUtils.readJsonStringFromClassPathResource("custom_events/job_body_no_text.json");
        
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
                .andExpect(MockMvcResultMatchers.content().string(CoreMatchers.startsWith("Request not valid because of invalid payload: Payload")));
        
    }
    
    @Test
    void testInvalidPayloadType() throws Exception {
        // given
        String validJobPostEvent = JsonUtils.readJsonStringFromClassPathResource("custom_events/job_body.json");
        
        //when
        ResultActions resp = mockMvc
                .perform(MockMvcRequestBuilders.post("/events")
                        .param("payloadType", "JOB_POST_NOT_EXISTING")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(validJobPostEvent)
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //then
        resp
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").value(CoreMatchers.startsWith("Unknown message type: JOB_POST_NOT_EXISTING")));
        
    }
    
    @Test
    void testJsonSchemaAbsent() throws Exception {
        // given
        String validJobPostEvent = JsonUtils.readJsonStringFromClassPathResource("custom_events/job_body.json");
        
        //when
        ResultActions resp = mockMvc
                .perform(MockMvcRequestBuilders.post("/events")
                        .param("payloadType", "BLOG")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(validJobPostEvent)
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //then
        resp
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").value(CoreMatchers.startsWith("java.io.FileNotFoundException: class path resource")));
        
    }
    
    @Test
    void testFindAllEvents() throws Exception {
        //GIVEN
        persistNTestEvents(5);
        
        //WHEN TEST REST
        ResultActions resp = mockMvc
                .perform(MockMvcRequestBuilders.get("/events")
                        .contentType(APPLICATION_JSON_UTF8)
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //THEN
        resp
                .andExpect(status().is2xxSuccessful());
        
        List<LinkedHashMap<?, ?>> events = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
        assertThat(events).hasSize(5);
    }
    
    @Test
    void testFilterTimeConsumedRangeEventsStartAndEnd() throws Exception {
        //GIVEN
        persistNTestEvents(5);

        //WHEN
        ResultActions resp = mockMvc
                .perform(MockMvcRequestBuilders.get("/events")
                        .param("consumedFrom", Instant.now().minus(20, ChronoUnit.MINUTES).toString())
                        .param("consumedTo", Instant.now().plus(150, ChronoUnit.MINUTES).toString())
                        .contentType(APPLICATION_JSON_UTF8)
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());

        //THEN
        resp
                .andExpect(status().is2xxSuccessful());
    
        List<LinkedHashMap<?, ?>> events = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
        assertThat(events).hasSize(3);
    }

    @Test
    void testFilterTimeConsumedRangeEventsStartOnly() throws Exception {
        //GIVEN
        persistNTestEvents(5);

        //WHEN
        ResultActions resp = mockMvc
                .perform(MockMvcRequestBuilders.get("/events")
                        .param("consumedFrom", Instant.now().plus(150, ChronoUnit.MINUTES).toString())
                        .contentType(APPLICATION_JSON_UTF8)
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());

        //THEN
        resp
                .andExpect(status().is2xxSuccessful());
    
        List<LinkedHashMap<?, ?>> events = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
        assertThat(events).hasSize(2);
    }
    
    @Test
    void testFilterTimeConsumedRangeEventsEndOnly() throws Exception {
        //GIVEN
        persistNTestEvents(5);
        
        //WHEN
        ResultActions resp = mockMvc
                .perform(MockMvcRequestBuilders.get("/events")
                        .param("consumedTo", Instant.now().plus(150, ChronoUnit.MINUTES).toString())
                        .contentType(APPLICATION_JSON_UTF8)
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //THEN
        resp
                .andExpect(status().is2xxSuccessful());
        
        List<LinkedHashMap<?, ?>> events = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
        assertThat(events).hasSize(3);
    }
    
    @Test
    void testFilterEventsByEventId() throws Exception {
        //GIVEN
        List<UUID> eventIds = persistNTestEvents(5);
    
        //WHEN
        ResultActions resp = mockMvc
                .perform(MockMvcRequestBuilders.get("/events")
                        .param("eventId", eventIds.get(0).toString())
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        //THEN
        resp
                .andExpect(status().is2xxSuccessful());
        
        List<LinkedHashMap<?, ?>> events = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
        assertThat(events).hasSize(1);
    }

    @Test
    void testGetPage1Size20() throws Exception {
        // GIVEN
        persistNTestEvents(19);

        //WHEN
        ResultActions resp = mockMvc
                .perform(MockMvcRequestBuilders.get("/events")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(APPLICATION_JSON_UTF8)
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
    
        List<LinkedHashMap<?, ?>> events = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
        assertThat(events).hasSize(19);
    }

    @Test
    void testGetPage1Size10() throws Exception {
        // GIVEN
        persistNTestEvents(19);

        //WHEN
        ResultActions resp = mockMvc
                .perform(MockMvcRequestBuilders.get("/events")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(APPLICATION_JSON_UTF8)
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
    
        List<LinkedHashMap<?, ?>> events = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
        assertThat(events).hasSize(10);
    }

    @Test
    void testGetPage2Size10() throws Exception {
        // GIVEN
        persistNTestEvents(19);

        //WHEN
        ResultActions resp = mockMvc
                .perform(MockMvcRequestBuilders.get("/events")
                        .param("page", "2")
                        .param("size", "10")
                        .contentType(APPLICATION_JSON_UTF8)
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
    
        List<LinkedHashMap<?, ?>> events = JsonPath.read(resp.andReturn().getResponse().getContentAsString(), "$.content");
        assertThat(events).hasSize(9);
    }
    
    @Test
    void testFindEventById() throws Exception {
        // GIVEN
        GenericEvent event = GenericEvent.builder()
                .id(UUID.randomUUID())
                .flowId("default-flow")
                .payloadJson(JsonUtils.readJsonNodeFromPojo(TestPayload.builder().value("Test").build()))
                .timeConsumed(Instant.now())
                .build();
        genericEventRepository.save(event);
    
        //WHEN
        ResultActions resp = mockMvc
                .perform(MockMvcRequestBuilders.get("/events/{eventId}", event.getId().toString())
                        .contentType(APPLICATION_JSON_UTF8)
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        // THEN
        resp
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").value(CoreMatchers.is(event.getId().toString())))
                .andExpect(jsonPath("$.payloadJson.value").value(CoreMatchers.is("Test")));
    }
    
    @Test
    void docFindEventById() throws Exception {
        // GIVEN
        GenericEvent event = GenericEvent.builder()
                .id(UUID.randomUUID())
                .flowId("default-flow")
                .payloadJson(JsonUtils.readJsonNodeFromPojo(
                        TestPayloadForDocs.builder()
                                .attribute1("Your payload value1. Can be anything")
                                .attribute2("Your payload value2. Can be anything")
                                .build()))
                .build();
        genericEventRepository.save(event);
    
        //WHEN
        mockMvc
                .perform(RestDocumentationRequestBuilders.get("/events/{eventId}", event.getId().toString())
                        .contentType(APPLICATION_JSON_UTF8)
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(
               		document("GET-events",
        			pathParameters(
                                        parameterWithName("eventId").description("Internal Notiflow event ID")
                                ),
                		responseFields( GenericEvent.fieldDesc ).andWithPrefix("payloadJson.",testPayloadFields)
                		)
                );
    }
    
    @Test
    void testFindEventByIdNotFound() throws Exception {
        // GIVEN
        GenericEvent event = GenericEvent.builder()
                .id(UUID.randomUUID())
                .flowId("default-flow")
                .payloadJson(JsonUtils.readJsonNodeFromPojo(TestPayload.builder().value("Test").build()))
                .timeConsumed(Instant.now())
                .build();
        genericEventRepository.save(event);
    
        UUID mismatchedId = getMismatchedId(event);
    
        //WHEN
        ResultActions resp = mockMvc
                .perform(MockMvcRequestBuilders.get("/events/{eventId}", mismatchedId.toString())
                        .contentType(APPLICATION_JSON_UTF8)
                        .accept(APPLICATION_JSON_UTF8))
                .andDo(MockMvcResultHandlers.print());
        
        // THEN
        resp
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindEventsForSummaryNotification() throws Exception {
        // given
        testDataController.persistFullEventProcessingData();
        Get.getJdbc().update("update nc_delivery_info set processed_on = now()");

        //THEN
        findEventsForNotif(0);
        
        //GIVEN notifyAfterProcessing = true
        GenericEvent testEvent = genericEventRepository.findById(TestDataRestController.EVENT_ID).get();
        testEvent.setNotifyAfterProcessing(true);
        genericEventRepository.save(testEvent);

        //THEN
        findEventsForNotif(0);

        //WHEN
        Get.getJdbc().update("update nc_delivery_info set processed_on = now() - INTERVAL '10 min'");

        //THEN
        List<JsonNode> events = findEventsForNotif(1);

        testEvent = JsonUtils.readObjectFromJSON(events.get(0), GenericEvent.class);
        assertThat(testEvent.getEventId()).isEqualTo(TestDataRestController.EVENT_ID);
    }

    private List<JsonNode> findEventsForNotif(int expectedCount) throws Exception, UnsupportedEncodingException {
        ResultActions resp = mockMvc
                .perform(MockMvcRequestBuilders.get("/events/summary-notification")
                .contentType(APPLICATION_JSON_UTF8)
        .accept(APPLICATION_JSON_UTF8))
        .andDo(MockMvcResultHandlers.print());

        //then no EVENT found
        resp.andExpect(status().is2xxSuccessful());

        List<JsonNode> events = JsonUtils.readJsonNodeListFromJSONString(resp.andReturn().getResponse().getContentAsString());
        assertThat(events).hasSize(expectedCount);

        return events;
    }

    
    private UUID getMismatchedId(GenericEvent event) {
        UUID mismatchedId = UUID.fromString(event.getId().toString());
        
        while (event.getId().equals(mismatchedId)) {
            mismatchedId = UUID.randomUUID();
        }
        
        return mismatchedId;
    }
    
    private List<UUID> persistNTestEvents(long n) {
        List<UUID> eventIds = new ArrayList<>();
        
        for (long i = 0; i < n; i++) {
            GenericEvent event = GenericEvent.builder()
                    .id(UUID.randomUUID())
                    .flowId("default-flow")
                    .payloadJson(JsonUtils.readJsonNodeFromJSONString("{}"))
                    .timeConsumed(Instant.now().plus(i * 60, ChronoUnit.MINUTES))
                    .build();
            genericEventRepository.save(event);
            eventIds.add(event.getId());
        }
        
        return eventIds;
    }
    
    @Data
    @Builder
    private static class TestPayload {
        String value;
    }
    
    @Data
    @Builder
    @JsonTypeInfo(use = Id.CLASS)
    private static class TestPayloadForDocs {
        String attribute1;
        String attribute2;
    }


}
