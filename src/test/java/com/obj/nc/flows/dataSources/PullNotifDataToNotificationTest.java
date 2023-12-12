package com.obj.nc.flows.dataSources;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.flows.dataSources.config.TestLicenceAgreementToNotificationConverter;
import com.obj.nc.flows.dataSources.config.TestLicenseAgreementProperties;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.internet.MimeMessage;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = {GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME, PullNotifDataToNotificationTest.DATA_SOURCE_POLLER_NAME})
@SpringBootTest(properties = {
        "nc.data-sources.jdbc[0].name=test-ds",
        "nc.data-sources.jdbc[0].url=jdbc:postgresql://localhost:25432/nc",
        "nc.data-sources.jdbc[0].username=nc",
        "nc.data-sources.jdbc[0].password=Ahoj123",
        "nc.data-sources.jdbc[0].jobs[0].name=check-agreements-expiry",
        "nc.data-sources.jdbc[0].jobs[0].sqlQuery=select * from license_agreement",
        "nc.data-sources.jdbc[0].jobs[0].pojoFCCN=com.obj.nc.flows.dataSources.config.TestLicenseAgreement",                
        "nc.data-sources.jdbc[0].jobs[0].cron=*/2 * * * * *",
        "test-license-agreements.admin-email=johndoe@objectify.sk",
        "test-license-agreements.email-template-path=agreements.html"
})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS) //this test register programmatically spring integration flows. it seems to confuse the spring context management in tests
class PullNotifDataToNotificationTest extends BaseIntegrationTest {

    @Autowired private JdbcTemplate springJdbcTemplate;
    
    @Qualifier(DATA_SOURCE_POLLER_NAME)
    @Autowired private SourcePollingChannelAdapter pollAbleSourceJdbc;

    public static final String DATA_SOURCE_POLLER_NAME = "NC_JDBC_DATA_SOURCE_test-ds_check-agreements-expiry_INTEGRATION_FLOW_POLLER";
    
    @BeforeEach
    public void setupDbs() {
        purgeNotifTables(springJdbcTemplate);
        
        springJdbcTemplate.update("drop table if exists license_agreement");
        springJdbcTemplate.execute("create table license_agreement (id varchar(10) not null,description text not null, expiry_date timestamptz not null); ");
        
        PullNotifDataToNotificationExpiryTest.persist10TestLicenseAgreements(springJdbcTemplate);

        pollAbleSourceJdbc.start();
    }
    
    @AfterEach
    public void tearDownDbs() {
        pollAbleSourceJdbc.stop(); 

        springJdbcTemplate.update("drop table if exists license_agreement");
    }
    
    @Test
    void testDataPulledAndMessageSent() {
        // when

        //then
        boolean received = greenMail.waitForIncomingEmail(15000L, 1);
    
        assertEquals(true, received);
        assertEquals(1, greenMail.getReceivedMessages().length); 
    
        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
        String body = GreenMailUtil.getBody(receivedMessage);
        assertThat(body).contains("Agreement 1", "Agreement 2", "Agreement 3", "Agreement 4",
                "Agreement 5", "Agreement 6", "Agreement 7", "Agreement 8", "Agreement 9");
    }
    
    
    @TestConfiguration
    public static class PullNotifDataToNotificationExpiryTestConfiguration {
        @Bean
        public TestLicenceAgreementToNotificationConverter pullNotifDataToNotificationConverter(TestLicenseAgreementProperties properties) {
            return new TestLicenceAgreementToNotificationConverter(properties);
        }
    }
    
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("no-reply@objectify.sk", "xxx"))
            .withPerMethodLifecycle(false);
    
}