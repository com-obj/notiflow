package com.obj.nc.koderia.mapper;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpMergeVarMapper;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpSenderConfigProperties;
import com.obj.nc.koderia.config.DomainConfig;
import com.obj.nc.koderia.domain.event.BaseKoderiaEvent;
import com.obj.nc.mappers.MailchimpContentMapper;
import com.obj.nc.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@RestClientTest
@ContextConfiguration(classes = {
        MailchimpSenderConfigProperties.class,
        MailchimpContentMapper.class,
        KoderiaMergeVarMapperImpl.class,
        DomainConfig.class
})
class MailchimpMailchimpContentMapperTest {

    public static final String EXPECTED_DTO_JSON_PATH = "mailchimp/send_message_dto.json";

    @Autowired
    private MailchimpContentMapper mapper;

    @Test
    void testMapWithTemplate() {
        // GIVEN
        BaseKoderiaEvent baseKoderiaEvent = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", BaseKoderiaEvent.class);
    
        MailchimpContent expectedContent = JsonUtils.readObjectFromClassPathResource(EXPECTED_DTO_JSON_PATH, MailchimpContent.class);

        // WHEN
        MailchimpContent actualContent = mapper.map(baseKoderiaEvent);

        // THEN
        assertThat(actualContent, equalTo(expectedContent));
    }

}