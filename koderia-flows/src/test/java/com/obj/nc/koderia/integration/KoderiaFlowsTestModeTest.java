package com.obj.nc.koderia.integration;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.config.InjectorConfiguration;
import com.obj.nc.config.JdbcConfiguration;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig;
import com.obj.nc.flows.inputEventRouting.config.InputEventRoutingProperties;
import com.obj.nc.functions.processors.eventFactory.GenericEventToNotificaitonIntentConverter;
import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromNotificationIntentProcessingFunction;
import com.obj.nc.functions.sink.inputPersister.GenericEventPersisterConsumer;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;
import com.obj.nc.koderia.KoderiaFlowsApplication;
import com.obj.nc.koderia.dto.koderia.event.BaseKoderiaEventDto;
import com.obj.nc.koderia.dto.koderia.recipients.RecipientDto;
import com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinderConfig;
import com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinderProcessorFunction;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import javax.mail.internet.MimeMessage;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinderConfig.RECIPIENTS_PATH;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest(properties = {
        "nc.flows.input-evet-routing.type=FLOW_ID",
        "nc.flows.test-mode.enabled=true",
        "nc.flows.test-mode.period-in-seconds=1",
        "nc.flows.test-mode.recipients=bazik@objectify.sk"
})
@DirtiesContext //have to dispose test mode green mail server
@Tag("test-mode")
public class KoderiaFlowsTestModeTest extends BaseIntegrationTest {
    @Qualifier(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
    @Autowired private SourcePollingChannelAdapter pollableSource;
    @Autowired private KoderiaRecipientsFinderProcessorFunction koderiaRecipientsFinder;
    @Autowired private KoderiaRecipientsFinderConfig koderiaRecipientsFinderConfig;
    @Autowired private GenericEventRepository genEventRepo;
    private MockRestServiceServer koderiaMockServer;
        
    @BeforeEach
    void cleanTables(@Autowired JdbcTemplate jdbcTemplate) throws FolderException {
        purgeNotifTables(jdbcTemplate);     
        greenMail.purgeEmailFromAllMailboxes();
        koderiaMockServer = MockRestServiceServer.bindTo(koderiaRecipientsFinder.getRestTemplate()).build();
        pollableSource.start();
    }
    @AfterEach
    public void stopSourcePolling() {
        pollableSource.stop();
    }
	
    @Test
    void testNotifyCustomersViaTestmodeEmail() {
        // GIVEN
        BaseKoderiaEventDto baseKoderiaEventDto = JsonUtils.readObjectFromClassPathResource("koderia/create_request/job_body.json", BaseKoderiaEventDto.class);
        GenericEvent genericEvent = GenericEvent.from(JsonUtils.writeObjectToJSONNode(baseKoderiaEventDto));
        genericEvent.setFlowId("default-flow");
        createRestCallExpectation();
        
    	//WHEN
    	genEventRepo.save(genericEvent);
    	
    	//THEN
        boolean success = greenMail.waitForIncomingEmail(60000, 1);
        
        Assertions.assertThat(success).isTrue();
        
        MimeMessage[] msgs = greenMail.getReceivedMessages();
        Assertions.assertThat(msgs.length).isEqualTo(1);
        
        System.out.println(GreenMailUtil.getWholeMessage(msgs[0]));

        assertMessagesContains(msgs, MailMessageForAssertions.as("bazik@objectify.sk" ,"Notifications digest while running test mode", 
                "john.doe@objectify.sk", "Business Intelligence (BI) Developer", "We are looking for a Business Intelligence (BI) Developer to create...",
                "john.dudly@objectify.sk", "Business Intelligence (BI) Developer", "We are looking for a Business Intelligence (BI) Developer to create...",
                "john.jovi@objectify.sk", "Business Intelligence (BI) Developer", "We are looking for a Business Intelligence (BI) Developer to create..."));
    }
    
    private void createRestCallExpectation() {
        String RECIPIENTS_JSON_PATH = "koderia/recipient_queries/job_recipients_response.json";
        RecipientDto[] responseBody = JsonUtils.readObjectFromClassPathResource(RECIPIENTS_JSON_PATH, RecipientDto[].class);
    
        koderiaMockServer.expect(ExpectedCount.once(),
                requestTo(koderiaRecipientsFinderConfig.getKoderiaApiUrl() + RECIPIENTS_PATH))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + koderiaRecipientsFinderConfig.getKoderiaApiToken()))
                .andExpect(jsonPath("$.type", equalTo("JOB_POST")))
                .andExpect(jsonPath("$.data.type", equalTo("Analytik")))
                .andExpect(jsonPath("$.data.technologies[0]", equalTo("Microsoft Power BI")))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(JsonUtils.writeObjectToJSONString(responseBody))
                );
    }
    
    @RegisterExtension
    protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
      	.withConfiguration(
      			GreenMailConfiguration.aConfig()
      			.withUser("no-reply@objectify.sk", "xxx"))
      	.withPerMethodLifecycle(true);
    
}

