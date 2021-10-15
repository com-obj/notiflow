package com.obj.nc.flows.dataSources;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.flows.dataSources.config.GenericDataToNotificationConverter;
import com.obj.nc.flows.dataSources.config.LicenseAgreement;
import com.obj.nc.flows.dataSources.config.LicenseAgreementProperties;
import com.obj.nc.functions.processors.genericDataConverter.ExtensionsBasedGenericData2NotificationConverter;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.sql.Timestamp.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest(properties = {
        "nc.data-sources.jdbc[0].name=test-ds",
        "nc.data-sources.jdbc[0].url=jdbc:postgresql://localhost:25432/nc",
        "nc.data-sources.jdbc[0].username=nc",
        "nc.data-sources.jdbc[0].password=ZMss4o9mdBLV",
        "nc.data-sources.jdbc[0].jobs[0].name=check-agreements-expiry",
        "nc.data-sources.jdbc[0].jobs[0].entity-name=license_agreement",
        "nc.data-sources.jdbc[0].jobs[0].cron=*/1 * * * * *",
        "nc.data-sources.jdbc[0].jobs[0].expiry-check.field-name=expiry_date",
        "nc.data-sources.jdbc[0].jobs[0].expiry-check.days-until-expiry=5",
        "license-agreements.admin-email=johndoe@objectify.sk",
        "license-agreements.email-template-path=agreements.html",
        "nc.functions.email-templates.templates-root-dir=src/test/resources/templates"
})
class GenericDataToNotificationExpiryTest extends BaseIntegrationTest {
    
    @Autowired private JdbcTemplate springJdbcTemplate;
    @Autowired private ExtensionsBasedGenericData2NotificationConverter extensionsBasedGenericData2NotificationConverter;
    
    @BeforeEach
    void setupDbs() {
        purgeNotifTables(springJdbcTemplate);
        springJdbcTemplate.update("drop table if exists license_agreement");
        springJdbcTemplate.execute("create table license_agreement (description text not null, expiry_date timestamptz not null); ");
        persistTestLicenseAgreements();
    }
    
    @AfterEach
    void tearDown() {
        springJdbcTemplate.update("drop table if exists license_agreement");
    }
    
    @Test
    void testDataPulledAndMessageSent() {
        // when
        boolean recieved = greenMail.waitForIncomingEmail(5000L, 1);
    
        assertEquals(true, recieved);
        assertEquals(1, greenMail.getReceivedMessages().length);
    
        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
        String body = GreenMailUtil.getBody(receivedMessage);
        assertThat(body).contains("Agreement 1", "Agreement 2", "Agreement 3", "Agreement 4");
    }
    
    private void persistTestLicenseAgreements() {
        List<LicenseAgreement> testAgreements = IntStream
                .range(1, 10)
                .mapToObj(i -> 
                        LicenseAgreement
                                .builder()
                                .description("Agreement ".concat(String.valueOf(i)))
                                .expiryDate(Instant.now().plus(i, ChronoUnit.DAYS))
                                .build())
                .collect(Collectors.toList());
    
        springJdbcTemplate.batchUpdate(
                "insert into license_agreement (description, expiry_date) values (?, ?) ", 
                new BatchPreparedStatementSetter() {
    
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        LicenseAgreement agreement = testAgreements.get(i);
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
        public GenericDataToNotificationConverter genericDataToNotificationConverter(LicenseAgreementProperties properties) {
            return new GenericDataToNotificationConverter(properties);
        }
    }
    
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("no-reply@objectify.sk", "xxx"))
            .withPerMethodLifecycle(false);
    
}