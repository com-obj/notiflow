package com.obj.nc.koderia.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.koderia.domain.event.BaseKoderiaEvent;
import com.obj.nc.utils.JsonUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@RestClientTest
@Import(MailchimpMessageMapperImplTestConfig.class)
class MailchimpKoderiaEvent2MailchimpContentMapperTest {

    public static final String EXPECTED_DTO_JSON_PATH = "mailchimp/send_message_dto.json";

    @Autowired
    private KoderiaEvent2MailchimpContentMapper mapper;

    @Test
    void testMapWithTemplate() throws JsonProcessingException {
        // GIVEN
        BaseKoderiaEvent baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", BaseKoderiaEvent.class);
    
        MailchimpContent expectedContent = JsonUtils.readObjectFromClassPathResource(EXPECTED_DTO_JSON_PATH, MailchimpContent.class);

        // WHEN
        MailchimpContent actualContent = mapper.map(baseKoderiaEvent);

        // THEN
        MatcherAssert.assertThat(JsonUtils.getObjectMapper().writeValueAsString(actualContent), Matchers.equalTo(JsonUtils.getObjectMapper().writeValueAsString(expectedContent)));
    }

}