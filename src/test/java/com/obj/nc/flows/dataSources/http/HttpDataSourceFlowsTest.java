/*
 * Copyright (C) 2021 the original author or authors.
 * This file is part of Notiflow
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.obj.nc.flows.dataSources.http;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.flows.dataSources.config.TestLicenceAgreementToNotificationConverter;
import com.obj.nc.flows.dataSources.config.TestLicenseAgreementProperties;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.mail.internet.MimeMessage;

import static com.obj.nc.config.PureRestTemplateConfig.PURE_REST_TEMPLATE;
import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ActiveProfiles(value = {"test"}, resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest(properties = {
        "nc.data-sources.http[0].name=TEST0-DS",
        "nc.data-sources.http[0].url=http://service-0/json",
        "nc.data-sources.http[0].pojoFCCN=com.obj.nc.flows.dataSources.config.TestLicenseAgreement",
        "nc.data-sources.http[0].cron=*/5 * * * * *",
        "nc.data-sources.http[0].externalIdAttrName=id",
        "test-license-agreements.admin-email=johndoe@objectify.sk",
        "test-license-agreements.email-template-path=agreements.html"
})
@SpringIntegrationTest(noAutoStartup = {GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
//this test register programmatically spring integration flows. it seems to confuse the spring context management in tests
public class HttpDataSourceFlowsTest {

    private MockRestServiceServer server;

    @Autowired
    @Qualifier(PURE_REST_TEMPLATE)
    RestTemplate pureRestTemplate;

    @BeforeEach
    void setUp() {
        server = MockRestServiceServer.bindTo(pureRestTemplate).build();
        server.expect(ExpectedCount.manyTimes(), requestTo("http://service-0/json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[{\"id\":\"faktura-01\", \"description\":\"Invoice 1\",\"expiry_date\":\"2021-11-05T11:32:45.757Z\"}]", MediaType.APPLICATION_JSON));
    }

    @Test
    void testContact() throws InterruptedException {
        // then
        boolean received = greenMail.waitForIncomingEmail(15000L, 1);

        Assertions.assertTrue(received);
        Assertions.assertEquals(1, greenMail.getReceivedMessages().length);

        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
        String body = GreenMailUtil.getBody(receivedMessage);
        assertThat(body).contains("Invoice 1");

        server.verify();
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
