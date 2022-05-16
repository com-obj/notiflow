package com.obj.nc.flows.dataSources.firestore;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.common.collect.ImmutableList;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.flows.dataSources.config.TestCvToNotificationConverter;
import com.obj.nc.flows.dataSources.config.TestLicenseAgreementProperties;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ActiveProfiles(value = {"test"}, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = {GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME})
@SpringBootTest(properties = {
        "nc.data-sources.firestore[0].name=test-ds",
        "nc.data-sources.firestore[0].serviceKeyPath=/test/path/file.json",
        "nc.data-sources.firestore[0].appName=test-app",
        "nc.data-sources.firestore[0].databaseUrl=https://test-database-url.com",
        "nc.data-sources.firestore[0].jobs[0].name=test-cvs",
        "nc.data-sources.firestore[0].jobs[0].collectionName=cv_view",
        "nc.data-sources.firestore[0].jobs[0].pojoFCCN=com.obj.nc.flows.dataSources.config.TestCv",
        "nc.data-sources.firestore[0].jobs[0].cron=*/2 * * * * *",
        "test-license-agreements.admin-email=johndoe@objectify.sk",
        "test-license-agreements.email-template-path=cvs.html"
})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS) //this test register programmatically spring integration flows. it seems to confuse the spring context management in tests
class FirestoreDataSourceFlowsTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate springJdbcTemplate;

    @BeforeEach
    void setup() {
        purgeNotifTables(springJdbcTemplate);
    }

    @Test
    void testDataPulledAndMessageSent() {
        boolean received = greenMail.waitForIncomingEmail(15000L, 1);

        assertEquals(true, received);
        assertEquals(1, greenMail.getReceivedMessages().length);

        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
        String body = GreenMailUtil.getBody(receivedMessage);
        assertThat(body).contains("cv-id-1");
    }

    @TestConfiguration
    public static class PullNotifDataToNotificationExpiryTestConfiguration {

        @SneakyThrows
        @Primary
        @Bean
        public FirestoreFactory firestoreFactory() {
            FirestoreFactory firestoreFactory = mock(FirestoreFactory.class);
            Firestore firestore = mock(Firestore.class);
            when(firestoreFactory.getFirestore(any())).thenReturn(firestore);

            CollectionReference collectionReference = mock(CollectionReference.class);
            when(firestore.collection(eq("cv_view"))).thenReturn(collectionReference);

            Query query = mock(Query.class);
            when(collectionReference.offset(eq(0))).thenReturn(query);

            ApiFuture<QuerySnapshot> apiFuture = mock(ApiFuture.class);
            when(query.get()).thenReturn(apiFuture);

            QuerySnapshot querySnapshot = mock(QuerySnapshot.class);
            when(apiFuture.get()).thenReturn(querySnapshot);

            QueryDocumentSnapshot cv1 = mock(QueryDocumentSnapshot.class);
            when(cv1.getId()).thenReturn("cv-id-1");

            Map<String, Object> cv1Params = new HashMap<>();
            cv1Params.put("updatedAt", Timestamp.ofTimeSecondsAndNanos(123, 111));
            when(cv1.getData()).thenReturn(cv1Params);

            when(querySnapshot.getDocuments()).thenReturn(ImmutableList.of(cv1));

            return firestoreFactory;
        }

        @Bean
        public TestCvToNotificationConverter pullNotifDataToNotificationConverter(TestLicenseAgreementProperties properties) {
            return new TestCvToNotificationConverter(properties);
        }
    }

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("no-reply@objectify.sk", "xxx"))
            .withPerMethodLifecycle(false);

}