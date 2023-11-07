package com.obj.nc.flows.eventSummaryNotification;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.Get;
import com.obj.nc.controllers.TestDataRestController;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.flows.dataSources.config.TestLicenceAgreementToNotificationConverter;
import com.obj.nc.flows.dataSources.config.TestLicenseAgreementProperties;
import com.obj.nc.flows.dataSources.http.HttpDatasourceNameCreator;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;

import javax.mail.internet.MimeMessage;

import static org.junit.Assert.assertEquals;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest(properties = {
        "nc.flows.event-summary-notif.event-selection=ALL_EVENT",
        "nc.flows.event-summary-notif.seconds-since-last-processing=1",
        "nc.flows.event-summary-notif.cron=*/12 * * * * *",        
        "nc.flows.event-summary-notif.email_recipients[0]=cuzy@objetify.sk",}
        , webEnvironment = WebEnvironment.DEFINED_PORT
)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS) //this test register programmatically spring integration flows. it seems to confuse the spring context management in tests
@Slf4j
class EventSummaryNotificationTest extends BaseIntegrationTest {

    @Autowired private JdbcTemplate springJdbcTemplate;
    @Autowired private TestDataRestController testDataController;
    @Autowired private GenericEventRepository eventRepo;

    @Qualifier(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
    @Autowired private SourcePollingChannelAdapter pollAbleChannel;
    
    @Qualifier(DATA_SOURCE_POLLER_NAME)
    @Autowired private SourcePollingChannelAdapter pollAbleSourceHttp;

    public static final String DATA_SOURCE_POLLER_NAME = 
        HttpDatasourceNameCreator.PULL_HTTP_DS_NAME_PREFIX+
        EventSummaryNotificationProperties.EVENT_SUMMARY_DS_NAME+
        HttpDatasourceNameCreator.PULL_HTTP_DS_JOB_POSTFIX + 
        HttpDatasourceNameCreator.PULL_HTTP_DS_POLLER_POSTFIX;
    
    @BeforeEach
    public void setupDbs() {
        purgeNotifTables(springJdbcTemplate);      

        pollAbleSourceHttp.start();  
    }
    
    @AfterEach
    public void tearDownDbs() {
        pollAbleSourceHttp.stop(); 
    }
    
    @Test
    void testDataPulledAndMessageSent() {
        // when
        GenericEvent testEvent = testDataController.persistFullEventProcessingData();
        testEvent.setNotifyAfterProcessing(true);
        eventRepo.save(testEvent);

        springJdbcTemplate.update("update nc_delivery_info set processed_on = now() - INTERVAL '25 hours'");

        //then
        boolean received = greenMail.waitForIncomingEmail(15000L, 1);   //this test can take some time
    
        assertEquals(true, received);
        assertEquals(1, greenMail.getReceivedMessages().length); 
    
        MimeMessage[] receivedMessage = greenMail.getReceivedMessages();
        //log.info(GreenMailUtil.getBody(receivedMessage[0]));

        assertMessagesSendTo(receivedMessage,"cuzy@objetify.sk", 1);
        assertMessagesContains(
            receivedMessage, 
            MailMessageForAssertions.as(
                "cuzy@objetify.sk", 
                "Summary delivery information for notification event", 
                "This is an informational email about the notification status",
                "EMAIL", "SMS",
                "0", "1", "2",
                "Message type", "Messages produced", "Number of recipients",
                "NC Processing", "NC Sent", "NC Failed", "NC Discarded", "Delivered",
                "Delivery Failed", "Delivery Unknown", "Delivery Pending"
            )
        );
    }
        
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("no-reply@objectify.sk", "xxx"))
            .withPerMethodLifecycle(false);
    
}