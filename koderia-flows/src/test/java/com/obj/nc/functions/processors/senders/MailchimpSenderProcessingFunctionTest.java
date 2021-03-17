package com.obj.nc.functions.processors.senders;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.AggregatedEmail;
import com.obj.nc.domain.message.Email;
import com.obj.nc.dto.mailchimp.MessageResponseDto;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.obj.nc.functions.processors.KoderiaEventConverterExecution.ORIGINAL_EVENT_FIELD;
import static com.obj.nc.functions.processors.senders.MailchimpSenderExecution.MAILCHIMP_RESPONSE_FIELD;
import static com.obj.nc.services.MailchimpRestClientImpl.SEND_TEMPLATE_PATH;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@RestClientTest
@Import(MailchimpSenderProcessingFunctionTestConfig.class)
class MailchimpSenderProcessingFunctionTest {

    public static final String RESPONSE_JSON_PATH = "mailchimp/response_body.json";
    public static final String MESSAGE_JSON_PATH = "mailchimp/message.json";
    public static final String AGGREGATE_MESSAGE_JSON_PATH = "mailchimp/aggregate_message.json";

    @Autowired
    private MailchimpSenderProcessingFunction sendMailchimpMessage;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void testSendMessageWithTemplate() {
        // WITH MOCK SERVER
        MessageResponseDto[] responseDtos = JsonUtils.readObjectFromClassPathResource(RESPONSE_JSON_PATH, MessageResponseDto[].class);
        String responseDtosJsonString = JsonUtils.writeObjectToJSONString(responseDtos);

        server.expect(once(), requestTo(SEND_TEMPLATE_PATH))
                .andRespond(withSuccess(responseDtosJsonString, MediaType.APPLICATION_JSON));

        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource(MESSAGE_JSON_PATH, Message.class);
        // FIX ABSOLUTE PATHS TO TEST FILES
        Email email = inputMessage.getContentTyped();
        email.getAttachments().forEach(attachement -> {
            try {
                attachement.setFileURI(new ClassPathResource(attachement.getFileURI().getPath()).getURI());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // WHEN
        Message outputMessage = sendMailchimpMessage.apply(inputMessage);

        // THEN
        server.verify();
        MatcherAssert.assertThat(outputMessage, Matchers.notNullValue());
        MessageResponseDto outputMessageResponseDto = JsonUtils.readClassFromObject(outputMessage.getBody().getAttributeValueAs(MAILCHIMP_RESPONSE_FIELD, List.class).get(0), MessageResponseDto.class);
        MatcherAssert.assertThat(outputMessageResponseDto, Matchers.equalTo(responseDtos[0]));
    }

    @Test
    void testSendAggregateMessageWithTemplate() {
        // WITH MOCK SERVER
        MessageResponseDto[] responseDtos = JsonUtils.readObjectFromClassPathResource(RESPONSE_JSON_PATH, MessageResponseDto[].class);
        String responseDtosJsonString = JsonUtils.writeObjectToJSONString(responseDtos);

        server.expect(once(), requestTo(SEND_TEMPLATE_PATH))
                .andRespond(withSuccess(responseDtosJsonString, MediaType.APPLICATION_JSON));

        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource(AGGREGATE_MESSAGE_JSON_PATH, Message.class);
        // FIX ABSOLUTE PATHS TO TEST FILES
        AggregatedEmail aggregateContent = inputMessage.getContentTyped();
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
        server.verify();
        MatcherAssert.assertThat(outputMessage, Matchers.notNullValue());
        MessageResponseDto outputMessageResponseDto = JsonUtils.readClassFromObject(outputMessage.getBody().getAttributeValueAs(MAILCHIMP_RESPONSE_FIELD, List.class).get(0), MessageResponseDto.class);
        MatcherAssert.assertThat(outputMessageResponseDto, Matchers.equalTo(responseDtos[0]));
    }

    @Test
    void testSendNullMessage() {
        // WITH MOCK SERVER
        MessageResponseDto[] responseDtos = JsonUtils.readObjectFromClassPathResource(RESPONSE_JSON_PATH, MessageResponseDto[].class);
        String responseDtosJsonString = JsonUtils.writeObjectToJSONString(responseDtos);

        server.expect(once(), requestTo(SEND_TEMPLATE_PATH))
                .andRespond(withSuccess(responseDtosJsonString, MediaType.APPLICATION_JSON));

        // GIVEN
        Message inputMessage = null;

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> sendMailchimpMessage.apply(inputMessage))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Message must not be null");
    }

    @Test
    void testSendMessageWithNoOriginalEvent() {
        // WITH MOCK SERVER
        MessageResponseDto[] responseDtos = JsonUtils.readObjectFromClassPathResource(RESPONSE_JSON_PATH, MessageResponseDto[].class);
        String responseDtosJsonString = JsonUtils.writeObjectToJSONString(responseDtos);

        server.expect(once(), requestTo(SEND_TEMPLATE_PATH))
                .andRespond(withSuccess(responseDtosJsonString, MediaType.APPLICATION_JSON));

        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource(MESSAGE_JSON_PATH, Message.class);
        // FIX ABSOLUTE PATHS TO TEST FILES
        AggregatedEmail aggregateContent = inputMessage.getContentTyped();
        aggregateContent.getAggregateContent().forEach(part -> {
            part.getAttachments().forEach(attachement -> {
                try {
                    attachement.setFileURI(new ClassPathResource(attachement.getFileURI().getPath()).getURI());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
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
        // WITH MOCK SERVER
        MessageResponseDto[] responseDtos = JsonUtils.readObjectFromClassPathResource(RESPONSE_JSON_PATH, MessageResponseDto[].class);
        String responseDtosJsonString = JsonUtils.writeObjectToJSONString(responseDtos);

        server.expect(once(), requestTo(SEND_TEMPLATE_PATH))
                .andRespond(withSuccess(responseDtosJsonString, MediaType.APPLICATION_JSON));

        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource(MESSAGE_JSON_PATH, Message.class);
        // FIX ABSOLUTE PATHS TO TEST FILES
        AggregatedEmail aggregateContent = inputMessage.getContentTyped();
        aggregateContent.getAggregateContent().forEach(part -> {
            part.getAttachments().forEach(attachement -> {
                try {
                    attachement.setFileURI(new ClassPathResource(attachement.getFileURI().getPath()).getURI());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
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
        MessageResponseDto[] responseDtos = JsonUtils.readObjectFromClassPathResource(RESPONSE_JSON_PATH, MessageResponseDto[].class);
        String responseDtosJsonString = JsonUtils.writeObjectToJSONString(responseDtos);

        server.expect(once(), requestTo(SEND_TEMPLATE_PATH))
                .andRespond(withSuccess(responseDtosJsonString, MediaType.APPLICATION_JSON));

        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource(MESSAGE_JSON_PATH, Message.class);
        // FIX ABSOLUTE PATHS TO TEST FILES
        AggregatedEmail aggregateContent = inputMessage.getContentTyped();
        aggregateContent.getAggregateContent().forEach(part -> {
            part.getAttachments().forEach(attachement -> {
                try {
                    attachement.setFileURI(new ClassPathResource(attachement.getFileURI().getPath()).getURI());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        aggregateContent.setSubject(param);

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> sendMailchimpMessage.apply(inputMessage))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Message must contain Subject with at least 1 non-whitespace character");
    }

    @Test
    void testSendMessageWithNoReceivingEndpoints() {
        // WITH MOCK SERVER
        MessageResponseDto[] responseDtos = JsonUtils.readObjectFromClassPathResource(RESPONSE_JSON_PATH, MessageResponseDto[].class);
        String responseDtosJsonString = JsonUtils.writeObjectToJSONString(responseDtos);

        server.expect(once(), requestTo(SEND_TEMPLATE_PATH))
                .andRespond(withSuccess(responseDtosJsonString, MediaType.APPLICATION_JSON));

        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource(MESSAGE_JSON_PATH, Message.class);
        // FIX ABSOLUTE PATHS TO TEST FILES
        
        AggregatedEmail aggregateContent = inputMessage.getContentTyped();
        aggregateContent.getAggregateContent().forEach(part -> {
            part.getAttachments().forEach(attachement -> {
                try {
                    attachement.setFileURI(new ClassPathResource(attachement.getFileURI().getPath()).getURI());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        inputMessage.getBody().setRecievingEndpoints(new ArrayList<>());

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> sendMailchimpMessage.apply(inputMessage))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Mailchimp can only send message")
                .hasMessageContaining("to 1 Email endpoint");
    }

    @Test
    void testSendMessageWithNonEmailReceivingEndpoints() {
        // WITH MOCK SERVER
        MessageResponseDto[] responseDtos = JsonUtils.readObjectFromClassPathResource(RESPONSE_JSON_PATH, MessageResponseDto[].class);
        String responseDtosJsonString = JsonUtils.writeObjectToJSONString(responseDtos);

        server.expect(once(), requestTo(SEND_TEMPLATE_PATH))
                .andRespond(withSuccess(responseDtosJsonString, MediaType.APPLICATION_JSON));

        // GIVEN
        Message inputMessage = JsonUtils.readObjectFromClassPathResource(MESSAGE_JSON_PATH, Message.class);
        // FIX ABSOLUTE PATHS TO TEST FILES
        
        AggregatedEmail aggregateContent = inputMessage.getContentTyped();
        aggregateContent.getAggregateContent().forEach(part -> {
            part.getAttachments().forEach(attachement -> {
                try {
                    attachement.setFileURI(new ClassPathResource(attachement.getFileURI().getPath()).getURI());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        inputMessage.getBody().setRecievingEndpoints(Arrays.asList(
                new RecievingEndpoint() {
                    @Override
                    public String getEndpointId() {
                        return "TEST";
                    }

                    @Override
                    public String getEndpointTypeName() {
                        return "TEST";
                    }
                }
        ));

        // WHEN - THEN
        Assertions.assertThatThrownBy(() -> sendMailchimpMessage.apply(inputMessage))
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Mailchimp can only send message")
                .hasMessageContaining("to 1 Email endpoint");
    }

}