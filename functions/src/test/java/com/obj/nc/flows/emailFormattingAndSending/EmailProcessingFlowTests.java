package com.obj.nc.flows.emailFormattingAndSending;

import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowProperties.MULTI_LOCALES_MERGE_STRATEGY.MERGE;
import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowProperties.MULTI_LOCALES_MERGE_STRATEGY.MESSAGE_PER_LOCALE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.mail.internet.MimeMessage;

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
import org.springframework.test.context.ActiveProfiles;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.EmailWithTemplatedContent;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest
@SpringBootTest(properties = {
        "nc.functions.email-tracking.read.enabled=true",
        "nc.functions.email-tracking.read.url=http://localhost:8080/email-tracking/read/{messageId}"
})
class EmailProcessingFlowTests extends BaseIntegrationTest {

    @Autowired private EmailProcessingFlowProperties properties;
    @Autowired private EmailProcessingFlow emailSendingFlow;
    
    @BeforeEach
    void setupGreenMail() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }
    
    @Test
    void testSendMultiLocaleMessagesTypeMerge() throws Exception {
        // given
        properties.setMultiLocalesMergeStrategy(MERGE);
        EmailWithTemplatedContent<?> inputMessage = JsonUtils.readObjectFromClassPathResource("messages/templated/teamplate_message_en_de.json", EmailWithTemplatedContent.class);
        emailSendingFlow.formatAndSend(inputMessage);
        // when
        boolean success = greenMail.waitForIncomingEmail(5000L, 1);
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
        EmailWithTemplatedContent<?> inputMessage = JsonUtils.readObjectFromClassPathResource("messages/templated/teamplate_message_en_de.json", EmailWithTemplatedContent.class);
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
        boolean success = greenMail.waitForIncomingEmail(5000L, 1);
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
        EmailMessage emailSent = emailSendingFlow.sendEmail(inputMessage).get(1, TimeUnit.SECONDS);
        // then
        assertThat(emailSent, CoreMatchers.notNullValue());
    }
    
    @Test
    void testSendTemaplatedEmailGateway() throws InterruptedException, ExecutionException, TimeoutException {
        // given
		//GIVEN
		String INPUT_JSON_FILE = "messages/templated/teamplate_message.json";
		EmailWithTemplatedContent inputMessage = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, EmailWithTemplatedContent.class);
        
        //when
        EmailMessage emailSent = emailSendingFlow.formatAndSend(inputMessage).get(1, TimeUnit.SECONDS);
        // then
        assertThat(emailSent, CoreMatchers.notNullValue());
    }
    
    @Test
    void testMessagesContainTrackingPxImage() throws Exception {
        // given
        properties.setMultiLocalesMergeStrategy(MERGE);
        EmailWithTemplatedContent<?> inputMessage = JsonUtils.readObjectFromClassPathResource("messages/templated/teamplate_message_en_de.json", EmailWithTemplatedContent.class);
        emailSendingFlow.formatAndSend(inputMessage);
        // when
        boolean success = greenMail.waitForIncomingEmail(5000L, 1);
        // then
        assertThat(success, equalTo(true));
    
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length, equalTo(1));
    
        MimeMessage receivedMessage = receivedMessages[0];
        MimeMessageParser parser = new MimeMessageParser(receivedMessage).parse();
        String receivedMessageContent = StringEscapeUtils.unescapeHtml4( parser.getHtmlContent() );
        Document contentAsDocument = Jsoup.parse(receivedMessageContent);
    
        List<Element> imgs = contentAsDocument.body().children().stream().filter(element -> element.is("img")).collect(Collectors.toList());
        assertThat(imgs.get(0).attr("src"), startsWith("http://localhost:8080/email-tracking/read/"));
        assertThat(imgs.get(1).attr("src"), startsWith("http://localhost:8080/email-tracking/read/"));
    }

    
    @RegisterExtension
    protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(
                    GreenMailConfiguration.aConfig()
                            .withUser("no-reply@objectify.sk", "xxx"))
            .withPerMethodLifecycle(true);
    
}