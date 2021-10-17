package com.obj.nc.flows.dataSources;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static java.sql.Timestamp.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.mail.internet.MimeMessage;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.flows.dataSources.config.TestLicenceAgreementToNotificationConverter;
import com.obj.nc.flows.dataSources.config.TestLicenseAgreement;
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
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest(properties = {
        "nc.data-sources.jdbc[0].name=test-ds",
        "nc.data-sources.jdbc[0].url=jdbc:postgresql://localhost:25432/nc",
        "nc.data-sources.jdbc[0].username=nc",
        "nc.data-sources.jdbc[0].password=ZMss4o9mdBLV",
        "nc.data-sources.jdbc[0].jobs[0].name=check-agreements-expiry",
        "nc.data-sources.jdbc[0].jobs[0].entity-name=license_agreement",
        "nc.data-sources.jdbc[0].jobs[0].sqlQuery=select * from license_agreement",  
        "nc.data-sources.jdbc[0].jobs[0].pojoFCCN=com.obj.nc.flows.dataSources.config.TestLicenseAgreement",     
        "nc.data-sources.jdbc[0].jobs[0].cron=*/1 * * * * *",
//        "nc.data-sources.jdbc[0].jobs[0].spel-filter-expression=expiryDate.isBefore(T(java.time.Instant).now().plus(5, T(java.time.temporal.ChronoUnit).DAYS))",        
        "nc.data-sources.jdbc[0].jobs[0].spel-filter-expression=isExpired(5)",        
        "test-license-agreements.admin-email=johndoe@objectify.sk",
        "test-license-agreements.email-template-path=agreements.html",
        "nc.functions.email-templates.templates-root-dir=src/test/resources/templates"
})
@SpringIntegrationTest(noAutoStartup = {GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME, GenericDataToNotificationTest.DATA_SOURCE_POLLER_NAME})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS) //this test register programmatically spring integration flows. it seems to confuse the spring context management in tests
class GenericDataToNotificationExpiryTest extends BaseIntegrationTest {
    
    @Autowired private JdbcTemplate springJdbcTemplate;

    @Qualifier(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
    @Autowired private SourcePollingChannelAdapter pollableSource;

    @Qualifier(GenericDataToNotificationTest.DATA_SOURCE_POLLER_NAME)
    @Autowired private SourcePollingChannelAdapter pollableSourceJdbc;
    
    @BeforeEach
    void setupDbs() {
        pollableSourceJdbc.stop(); //somehow this doesn't stop with the standard annotation
        purgeNotifTables(springJdbcTemplate);        
        
        springJdbcTemplate.update("drop table if exists license_agreement");
        springJdbcTemplate.execute("create table license_agreement (description text not null, expiry_date timestamptz not null); ");
        
        persistTestLicenseAgreements(springJdbcTemplate);
    }
    
    @AfterEach
    void tearDown() {
        springJdbcTemplate.update("drop table if exists license_agreement");
    }
    
    @Test
    void testDataPulledAndMessageSent() {
        // when
        pollableSourceJdbc.start();

        // then
        boolean received = greenMail.waitForIncomingEmail(5000L, 1);
    
        assertEquals(true, received);
        assertEquals(1, greenMail.getReceivedMessages().length);
    
        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
        String body = GreenMailUtil.getBody(receivedMessage);
        assertThat(body).contains("Agreement 1", "Agreement 2", "Agreement 3", "Agreement 4");
    }
    
    public static void persistTestLicenseAgreements(JdbcTemplate springJdbcTemplate) {
        List<TestLicenseAgreement> testAgreements = IntStream
                .range(1, 10)
                .mapToObj(i -> 
                        TestLicenseAgreement
                                .builder()
                                .description("Agreement ".concat(String.valueOf(i)))
                                .expiryDate(Instant.now().plus(i, ChronoUnit.DAYS))
                                .build())
                .collect(Collectors.toList());
    
        springJdbcTemplate.batchUpdate(
                "insert into license_agreement (description, expiry_date) values (?, ?) ", 
                new BatchPreparedStatementSetter() {
    
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        TestLicenseAgreement agreement = testAgreements.get(i);
                        ps.setString(1, agreement.getDescription());
                        ps.setTimestamp(2, from(agreement.getExpiryDate()));
                    }
            
                    public int getBatchSize() {
                        return testAgreements.size();
                    }
    
        });
    }
    
    @TestConfiguration
    public static class GenericDataToNotificationExpiryTestConfiguration {
        @Bean
        public TestLicenceAgreementToNotificationConverter genericDataToNotificationConverter(TestLicenseAgreementProperties properties) {
            return new TestLicenceAgreementToNotificationConverter(properties);
        }
    }
    
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("no-reply@objectify.sk", "xxx"))
            .withPerMethodLifecycle(false);
    
}