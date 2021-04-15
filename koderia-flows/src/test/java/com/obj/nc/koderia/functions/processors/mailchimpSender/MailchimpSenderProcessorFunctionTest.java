package com.obj.nc.koderia.functions.processors.mailchimpSender;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.content.email.AggregatedEmailContent;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.flows.testmode.mailchimp.dto.MessageResponseDto;
import com.obj.nc.functions.processors.senders.MailchimpSender;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.obj.nc.functions.processors.eventFactory.GenericEventToNotificaitonIntentConverter.ORIGINAL_EVENT_FIELD;
import static com.obj.nc.koderia.functions.processors.mailchimpSender.MailchimpSenderConfig.MAILCHIMP_RESPONSE_FIELD;
import static com.obj.nc.koderia.functions.processors.mailchimpSender.MailchimpSenderConfig.SEND_TEMPLATE_PATH;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@RestClientTest(MailchimpSenderProcessorFunction.class)
@Import(MailchimpSenderProcessorFunctionTestConfig.class)
class MailchimpSenderProcessorFunctionTest {

    public static final String RESPONSE_JSON_PATH = "mailchimp/response_body.json";
    public static final String MESSAGE_JSON_PATH = "mailchimp/message.json";
    public static final String AGGREGATE_MESSAGE_JSON_PATH = "mailchimp/aggregate_message.json";

    @Autowired private MailchimpSenderProcessorFunction sendMailchimpMessage;
    @Autowired private MailchimpSenderConfig mailchimpSenderConfig;
    @Autowired private MockRestServiceServer mailchimpMockServer;
    
    @BeforeEach
    void setUp(TestInfo testInfo) {
        if(testInfo.getTags().contains("aggregate")) {
            createAggregateMessageRestServerExpectations();
        } else {
            createSimpleMessageRestServerExpectations();
        }
    }
    
    @Test
    void testSendMessageWithTemplate() {
        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource(MESSAGE_JSON_PATH, Message.class);
        // FIX ABSOLUTE PATHS TO TEST FILES
        EmailContent emailContent = inputMessage.getContentTyped();
        emailContent.getAttachments().forEach(attachement -> {
            try {
                attachement.setFileURI(new ClassPathResource(attachement.getFileURI().getPath()).getURI());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // WHEN
        Message outputMessage = sendMailchimpMessage.apply(inputMessage);

        // THEN
        MatcherAssert.assertThat(outputMessage, Matchers.notNullValue());
        MessageResponseDto outputMessageResponseDto = JsonUtils.readClassFromObject(outputMessage.getBody().getAttributeValueAs(MAILCHIMP_RESPONSE_FIELD, List.class).get(0), MessageResponseDto.class);
        MessageResponseDto[] responseDtos = JsonUtils.readObjectFromClassPathResource(RESPONSE_JSON_PATH, MessageResponseDto[].class);
        MatcherAssert.assertThat(outputMessageResponseDto, Matchers.equalTo(responseDtos[0]));
    }

    @Test
    @Tag("aggregate")
    void testSendAggregateMessageWithTemplate() {
        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource(AGGREGATE_MESSAGE_JSON_PATH, Message.class);
        // FIX ABSOLUTE PATHS TO TEST FILES
        AggregatedEmailContent aggregateContent = inputMessage.getContentTyped();
        aggregateContent.getAggregateContent().forEach(part -> {
            part.getAttachments().forEach(attachement -> {
                try {
                    attachement.setFileURI(new ClassPathResource(attachement.getFileURI().getPath()).getURI());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        // WHEN
        Message outputMessage = sendMailchimpMessage.apply(inputMessage);

        // THEN
        MatcherAssert.assertThat(outputMessage, Matchers.notNullValue());
        MessageResponseDto outputMessageResponseDto = JsonUtils.readClassFromObject(outputMessage.getBody().getAttributeValueAs(MAILCHIMP_RESPONSE_FIELD, List.class).get(0), MessageResponseDto.class);
        MessageResponseDto[] responseDtos = JsonUtils.readObjectFromClassPathResource(RESPONSE_JSON_PATH, MessageResponseDto[].class);
        MatcherAssert.assertThat(outputMessageResponseDto, Matchers.equalTo(responseDtos[0]));
    }

    @Test
    void testSendNullMessage() {
        // GIVEN
        Message inputMessage = null;

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> sendMailchimpMessage.apply(inputMessage))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Message must not be null");
    }

    @Test
    void testSendMessageWithNoOriginalEvent() {
        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource(MESSAGE_JSON_PATH, Message.class);
        // FIX ABSOLUTE PATHS TO TEST FILES
        EmailContent emailContentContent = inputMessage.getContentTyped();
        emailContentContent.getAttachments().forEach(attachement -> {
            try {
                attachement.setFileURI(new ClassPathResource(attachement.getFileURI().getPath()).getURI());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        inputMessage.getBody().getMessage().getAttributes().remove(ORIGINAL_EVENT_FIELD);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> sendMailchimpMessage.apply(inputMessage))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Message must contain attribute:")
                .hasMessageNotContaining("type");
    }

    @Test
    void testSendMessageWithNoOriginalEventType() {
        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource(MESSAGE_JSON_PATH, Message.class);
        // FIX ABSOLUTE PATHS TO TEST FILES
        EmailContent emailContentContent = inputMessage.getContentTyped();
        emailContentContent.getAttachments().forEach(attachement -> {
            try {
                attachement.setFileURI(new ClassPathResource(attachement.getFileURI().getPath()).getURI());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        ((Map<?, ?>) inputMessage.getBody().getMessage().getAttributes().get(ORIGINAL_EVENT_FIELD)).remove("type");

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> sendMailchimpMessage.apply(inputMessage))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Message must contain attribute:")
                .hasMessageContaining("type");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t\t\n "})
    void testSendMessageWithNoTextSubject(String param) {
        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource(MESSAGE_JSON_PATH, Message.class);
        // FIX ABSOLUTE PATHS TO TEST FILES
        EmailContent emailContentContent = inputMessage.getContentTyped();
        emailContentContent.getAttachments().forEach(attachement -> {
            try {
                attachement.setFileURI(new ClassPathResource(attachement.getFileURI().getPath()).getURI());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        emailContentContent.setSubject(param);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> sendMailchimpMessage.apply(inputMessage))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Message must contain Subject with at least 1 non-whitespace character");
    }

    @Test
    void testSendMessageWithNoReceivingEndpoints() {
        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource(MESSAGE_JSON_PATH, Message.class);
        // FIX ABSOLUTE PATHS TO TEST FILES
        
        EmailContent emailContentContent = inputMessage.getContentTyped();
        emailContentContent.getAttachments().forEach(attachement -> {
            try {
                attachement.setFileURI(new ClassPathResource(attachement.getFileURI().getPath()).getURI());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        inputMessage.getBody().setRecievingEndpoints(new ArrayList<>());

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> sendMailchimpMessage.apply(inputMessage))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Mailchimp can only send message")
                .hasMessageContaining("to 1 EmailContent endpoint");
    }

    @Test
    void testSendMessageWithNonEmailReceivingEndpoints() {
        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource(MESSAGE_JSON_PATH, Message.class);
        // FIX ABSOLUTE PATHS TO TEST FILES
        
        EmailContent emailContentContent = inputMessage.getContentTyped();
        emailContentContent.getAttachments().forEach(attachement -> {
            try {
                attachement.setFileURI(new ClassPathResource(attachement.getFileURI().getPath()).getURI());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        inputMessage.getBody().setRecievingEndpoints(Arrays.asList(
                new RecievingEndpoint() {
                    @Override
                    public String getEndpointId() {
                        return "TEST";
                    }

                    @Override
                    public String getEndpointType() {
                        return "TEST";
                    }

					@Override
					public void setEndpointId(String endpointId) {
					}
                }
        ));

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> sendMailchimpMessage.apply(inputMessage))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Mailchimp can only send message")
                .hasMessageContaining("to 1 EmailContent endpoint");
    }
    
    
    private void createSimpleMessageRestServerExpectations() {
        String RESPONSE_JSON_PATH = "mailchimp/response_body.json";
        MessageResponseDto[] responseDtos = JsonUtils.readObjectFromClassPathResource(RESPONSE_JSON_PATH, MessageResponseDto[].class);
        String responseDtosJsonString = JsonUtils.writeObjectToJSONString(responseDtos);
        
        mailchimpMockServer.expect(times(1),
                requestTo(SEND_TEMPLATE_PATH))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + mailchimpSenderConfig.getMailchimpApi().getAuthKey()))
                .andExpect(jsonPath("$.key", equalTo("MOCKkey")))
                .andExpect(jsonPath("$.message.subject", anyOf(equalTo("Business Intelligence (BI) Developer"), equalTo("Koderia digest"))))
                .andExpect(jsonPath("$.message.merge_language", equalTo("handlebars")))
                .andExpect(jsonPath("$.message.global_merge_vars[0].name", equalTo("SIMPLE_MESSAGE")))
                .andExpect(jsonPath("$.message.global_merge_vars[0].content.type", equalTo("JOB_POST")))
                .andExpect(jsonPath("$.message.attachments[0].name", equalTo("test1.txt")))
                .andRespond(withSuccess(responseDtosJsonString, MediaType.APPLICATION_JSON));
    }
    
    private void createAggregateMessageRestServerExpectations() {
        String RESPONSE_JSON_PATH = "mailchimp/response_body.json";
        MessageResponseDto[] responseDtos = JsonUtils.readObjectFromClassPathResource(RESPONSE_JSON_PATH, MessageResponseDto[].class);
        String responseDtosJsonString = JsonUtils.writeObjectToJSONString(responseDtos);
        
        mailchimpMockServer.expect(times(1),
                requestTo(SEND_TEMPLATE_PATH))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + mailchimpSenderConfig.getMailchimpApi().getAuthKey()))
                .andExpect(jsonPath("$.key", equalTo("MOCKkey")))
                .andExpect(jsonPath("$.message.subject", equalTo("Koderia digest")))
                .andExpect(jsonPath("$.message.merge_language", equalTo("handlebars")))
                .andExpect(jsonPath("$.message.global_merge_vars[0].name", anyOf(equalTo("JOB_POST"), equalTo("NEWS"),
                        equalTo("LINK"), equalTo("EVENT"), equalTo("BLOG"))))
                .andExpect(jsonPath("$.message.global_merge_vars[1].name", anyOf(equalTo("JOB_POST"), equalTo("NEWS"),
                        equalTo("LINK"), equalTo("EVENT"), equalTo("BLOG"))))
                .andExpect(jsonPath("$.message.global_merge_vars[2].name", anyOf(equalTo("JOB_POST"), equalTo("NEWS"),
                        equalTo("LINK"), equalTo("EVENT"), equalTo("BLOG"))))
                .andExpect(jsonPath("$.message.global_merge_vars[3].name", anyOf(equalTo("JOB_POST"), equalTo("NEWS"),
                        equalTo("LINK"), equalTo("EVENT"), equalTo("BLOG"))))
                .andExpect(jsonPath("$.message.global_merge_vars[4].name", anyOf(equalTo("JOB_POST"), equalTo("NEWS"),
                        equalTo("LINK"), equalTo("EVENT"), equalTo("BLOG"))))
                .andRespond(withSuccess(responseDtosJsonString, MediaType.APPLICATION_JSON));
    }
    
}