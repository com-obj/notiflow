package com.obj.nc.services;

import com.obj.nc.domain.event.Event;
import com.obj.nc.dto.CreateJobPostDto;
import com.obj.nc.utils.JsonUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Map;

@SpringJUnitConfig(classes = EventServiceImplTestConfig.class)
class EventServiceImplTest {

    @Autowired
    private EventServiceImpl eventService;

    @Test
    void testMapValidCreateDto() {
        // given
        CreateJobPostDto createJobPostDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", CreateJobPostDto.class);

        // when
        Event mappedEvent = eventService.createEvent(createJobPostDto);

        // then
        MatcherAssert.assertThat(mappedEvent.getHeader().getConfigurationName(), Matchers.equalTo("koderia-pipeline"));
        MatcherAssert.assertThat(mappedEvent.getBody().getMessage().getContent().getSubject(), Matchers.equalTo(createJobPostDto.getSubject()));
        MatcherAssert.assertThat(mappedEvent.getBody().getMessage().getContent().getText(), Matchers.equalTo(createJobPostDto.getText()));

        // and
        Map<String, Object> eventAttributes = mappedEvent.getBody().getAttributes();
        MatcherAssert.assertThat(eventAttributes.get("id"), Matchers.equalTo(createJobPostDto.getId()));
        MatcherAssert.assertThat(eventAttributes.get("location"), Matchers.equalTo(createJobPostDto.getLocation()));
        MatcherAssert.assertThat(eventAttributes.get("rate"), Matchers.equalTo(createJobPostDto.getRate()));
        MatcherAssert.assertThat(eventAttributes.get("technologies"), Matchers.equalTo(createJobPostDto.getTechnologies()));
        MatcherAssert.assertThat(eventAttributes.get("specialRate"), Matchers.equalTo(createJobPostDto.getSpecialRate()));
        MatcherAssert.assertThat(eventAttributes.get("labels"), Matchers.equalTo(createJobPostDto.getLabels()));
        MatcherAssert.assertThat(eventAttributes.get("positionType"), Matchers.equalTo(createJobPostDto.getPositionType()));
        MatcherAssert.assertThat(eventAttributes.get("duration"), Matchers.equalTo(createJobPostDto.getDuration()));
        MatcherAssert.assertThat(eventAttributes.get("jobType"), Matchers.equalTo(createJobPostDto.getJobType()));
        MatcherAssert.assertThat(eventAttributes.get("dateOfStart"), Matchers.equalTo(createJobPostDto.getDateOfStart()));
    }

}