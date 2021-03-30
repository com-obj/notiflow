package com.obj.nc.mapper;

import com.obj.nc.KoderiaFlowsApplication;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.content.AggregatedEmail;
import com.obj.nc.domain.message.Message;
import com.obj.nc.dto.mailchimp.SendMessageWithTemplateDto;
import com.obj.nc.utils.JsonUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@JsonTest
@Import(MailchimpMessageMapperAggregateImplTestConfig.class)
@ContextConfiguration(classes = KoderiaFlowsApplication.class)
class MailchimpMessageMapperAggregateImplTest {

    public static final String MESSAGE_JSON_PATH = "mailchimp/aggregate_message.json";
    public static final String EXPECTED_DTO_JSON_PATH = "mailchimp/send_aggregate_message_dto.json";

    @Autowired
    @Qualifier(MailchimpMessageMapperAggregateImpl.COMPONENT_NAME)
    private MailchimpMessageMapperAggregateImpl mapper;

    @Test
    void testMapWithTemplate() {
        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource(MESSAGE_JSON_PATH, Message.class);
        // FIX ABSOLUTE PATHS TO TEST FILES
        AggregatedEmail aggregatedContent = inputMessage.getContentTyped();
        aggregatedContent.getAggregateContent().forEach(part -> {
        	part.getAttachments().forEach(attachement -> {
                try {
                    attachement.setFileURI(new ClassPathResource(attachement.getFileURI().getPath()).getURI());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        SendMessageWithTemplateDto expectedSendMessageDto = JsonUtils.readObjectFromClassPathResource(EXPECTED_DTO_JSON_PATH, SendMessageWithTemplateDto.class);

        // WHEN
        SendMessageWithTemplateDto sendMessageDto = mapper.mapWithTemplate(inputMessage);

        // THEN
        MatcherAssert.assertThat(sendMessageDto, Matchers.equalTo(expectedSendMessageDto));
    }

}