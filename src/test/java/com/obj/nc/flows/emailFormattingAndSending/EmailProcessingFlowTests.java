/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.flows.emailFormattingAndSending;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.config.NcAppConfigProperties;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.EmailMessageTemplated;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.apache.commons.text.StringEscapeUtils;
import org.hamcrest.CoreMatchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.internet.MimeMessage;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowProperties.MULTI_LOCALES_MERGE_STRATEGY.MERGE;
import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowProperties.MULTI_LOCALES_MERGE_STRATEGY.MESSAGE_PER_LOCALE;
import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;


@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
@SpringBootTest(properties = {
        "nc.functions.tracking.enabled=true"
})
class EmailProcessingFlowTests extends BaseIntegrationTest {

    @Autowired private EmailProcessingFlowProperties properties;
    @Autowired private EmailProcessingFlow emailSendingFlow;
    @Autowired private NcAppConfigProperties ncAppConfigProperties;
    
//	@Qualifier(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME)
//	@Autowired private SourcePollingChannelAdapter pollableSource;
    
    @BeforeEach
    void setupGreenMail(@Autowired JdbcTemplate jdbcTemplate) throws FolderException {
        purgeNotifTables(jdbcTemplate);
        greenMail.purgeEmailFromAllMailboxes();
        
//    	pollableSource.start();
    }
    
    @Test
    void testSendMultiLocaleMessagesTypeMerge() {
        // given
        properties.setMultiLocalesMergeStrategy(MERGE);
        EmailMessageTemplated<?> inputMessage = JsonUtils.readObjectFromClassPathResource("messages/templated/teamplate_message_en_de.json", EmailMessageTemplated.class);
        emailSendingFlow.formatAndSend(inputMessage);
        // when
        boolean success = greenMail.waitForIncomingEmail(15000L, 1);
        // then
        assertThat(success, equalTo(true));
    
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length, equalTo(1));
    
        assertMessagesContains(receivedMessages, MailMessageForAssertions.as("john.doe@objectify.sk", "Subject / Subject", 
                "Hallo World", "John Doe", "<img", "<br>", "Grues gott", "John Doe", "<img"));
    }
    
    @Test
    void testSendMultiLocaleMessagesTypeMessagePerLocale() {
        // given
        properties.setMultiLocalesMergeStrategy(MESSAGE_PER_LOCALE);
        EmailMessageTemplated<?> inputMessage = JsonUtils.readObjectFromClassPathResource("messages/templated/teamplate_message_en_de.json", EmailMessageTemplated.class);
        emailSendingFlow.formatAndSend(inputMessage);
        // when
        boolean success = greenMail.waitForIncomingEmail(10000L, 2);
        // then
        assertThat(success, equalTo(true));
        
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length, equalTo(2));
    }
    
    @Test
    void testSendNonTemplatedEmailMessage() {
        // given
        properties.setMultiLocalesMergeStrategy(MERGE);
        EmailMessage inputMessage = JsonUtils.readObjectFromClassPathResource("messages/simple_email_message.json", EmailMessage.class);
        emailSendingFlow.sendEmail(inputMessage);
        // when
        boolean success = greenMail.waitForIncomingEmail(15000L, 1);
        // then
        assertThat(success, equalTo(true));
        
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length, equalTo(1));
    }
    
    @Test
    void testSendEmailGateway() throws InterruptedException, ExecutionException, TimeoutException {
        // given
        EmailMessage inputMessage = JsonUtils.readObjectFromClassPathResource("messages/simple_email_message.json", EmailMessage.class);
        
        //when
        EmailMessage emailSent = emailSendingFlow.sendEmail(inputMessage)
        		.get(5, TimeUnit.SECONDS);
        // then
        assertThat(emailSent, CoreMatchers.notNullValue());
    }
    
    @Test
    void testSendTemaplatedEmailGateway() throws InterruptedException, ExecutionException, TimeoutException {
        // given
		//GIVEN
		String INPUT_JSON_FILE = "messages/templated/teamplate_message.json";
		EmailMessageTemplated<?> inputMessage = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, EmailMessageTemplated.class);
        
        //when
        EmailMessage emailSent = emailSendingFlow.formatAndSend(inputMessage).get(15, TimeUnit.SECONDS);
        // then
        assertThat(emailSent, CoreMatchers.notNullValue());
    }
    
    @Test
    void testMessagesContainTrackingPxImage() throws Exception {
        // given
        properties.setMultiLocalesMergeStrategy(MERGE);
        EmailMessageTemplated<?> inputMessage = JsonUtils.readObjectFromClassPathResource("messages/templated/teamplate_message_en_de.json", EmailMessageTemplated.class);
        emailSendingFlow.formatAndSend(inputMessage);
        // when
        boolean success = greenMail.waitForIncomingEmail(15000L, 1);
        // then
        assertThat(success, equalTo(true));
    
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length, equalTo(1));
    
        MimeMessage receivedMessage = receivedMessages[0];
        MimeMessageParser parser = new MimeMessageParser(receivedMessage).parse();
        String receivedMessageContent = StringEscapeUtils.unescapeHtml4( parser.getHtmlContent() );
        Document contentAsDocument = Jsoup.parse(receivedMessageContent);
    
        List<Element> imgs = contentAsDocument.body().children().stream().filter(element -> element.is("img")).collect(Collectors.toList());
        assertThat(imgs, hasSize(1));
        assertThat(imgs.get(0).attr("src"), startsWith(ncAppConfigProperties.getUrl()+"/delivery-info/messages/"));
        assertThat(imgs.get(0).attr("src"), endsWith("/mark-as-read"));
    }

//    @AfterEach
//    public void stopSourcePolling() {
//    	pollableSource.stop();
//    }
//    
    @RegisterExtension
    protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(
                    GreenMailConfiguration.aConfig()
                            .withUser("no-reply@objectify.sk", "xxx"))
            .withPerMethodLifecycle(true);
    
}