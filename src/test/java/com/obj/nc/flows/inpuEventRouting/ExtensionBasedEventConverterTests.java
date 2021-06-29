package com.obj.nc.flows.inpuEventRouting;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static com.obj.nc.flows.messageProcessing.MessageProcessingFlowConfig.MESSAGE_PROCESSING_FLOW_INPUT_CHANNEL_ID;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.mail.MessagingException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.domain.IsTypedJson;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.flows.inputEventRouting.extensions.GenericEvent2MessageProcessorExtension;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersisterConsumer;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest
public class ExtensionBasedEventConverterTests extends BaseIntegrationTest {
	
	@Autowired private GenericEventPersisterConsumer persister;
	
	@Qualifier(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
	@Autowired private SourcePollingChannelAdapter pollableSource;

	@Qualifier(MESSAGE_PROCESSING_FLOW_INPUT_CHANNEL_ID)
	@Autowired private PublishSubscribeChannel messageProcessingInputChannel;
	
	@Autowired private JdbcTemplate jdbcTemplate;

	
    @BeforeEach
    public void startSourcePolling() {
    	purgeNotifTables(jdbcTemplate);
    	
    	pollableSource.start();    	
    	
    	JsonUtils.resetObjectMapper();
    	JsonUtils.getObjectMapper().addMixIn(IsTypedJson.class, TestPayload.class);
    }
	
    @Test
    void testGenericEventRouting() throws MessagingException {
    	//GIVEN
    	TestPayload pyload = new TestPayload(1, "value");
    	
        GenericEvent event = GenericEvent.builder()
        		.id(UUID.randomUUID())
        		.flowId(UUID.randomUUID().toString())
        		.payloadJson(JsonUtils.writeObjectToJSONNode(pyload))
        		.build();
        
        //WHEN
        persister.accept(event);
             
        //THEN
        awaitSent(event.getId(), 1, Duration.ofSeconds(5));
    }
    
    @AfterEach
    public void stopSourcePolling() {
    	pollableSource.stop();
    }

    @TestConfiguration
    public static class EventToMessageExtensionConfiguration {

        @Bean
        public GenericEvent2MessageProcessorExtension event2Message() {
            return new GenericEvent2MessageProcessorExtension () {

				@Override
				public Optional<PayloadValidationException> checkPreCondition(GenericEvent payload) {
					if (!(payload.getPayloadAsPojo() instanceof TestPayload)) {
						return Optional.of(new PayloadValidationException("No test payload"));
					}
					return Optional.empty();
				}

				@Override
				public List<com.obj.nc.domain.message.Message<?>> convertEvent(GenericEvent event) {					
					EmailMessage email1 = new EmailMessage();
					email1.addRecievingEndpoints(EmailEndpoint.builder().email("test@objectify.sk").build());
					email1.getBody().setSubject("Subject");
					email1.getBody().setText("text");

					List<com.obj.nc.domain.message.Message<?>> msg = Arrays.asList(email1);

					return msg;
				}            	
            };
        }
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonTypeInfo(use = Id.CLASS)
    public static class TestPayload implements IsTypedJson {
    	
    	private Integer num;
    	private String str;
    }
    
    @RegisterExtension
    protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
      	.withConfiguration(
      			GreenMailConfiguration.aConfig()
      			.withUser("no-reply@objectify.sk", "xxx"))
      	.withPerMethodLifecycle(true);
}
