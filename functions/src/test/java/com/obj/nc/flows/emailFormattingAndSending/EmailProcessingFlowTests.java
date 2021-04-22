package com.obj.nc.flows.emailFormattingAndSending;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.message.Message;
import com.obj.nc.utils.JsonUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ActiveProfiles;


import javax.mail.internet.MimeMessage;

import java.util.List;

import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowConfig.EMAIL_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowProperties.MULTI_LOCALES_MERGE_STRATEGY.MERGE;
import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowProperties.MULTI_LOCALES_MERGE_STRATEGY.MESSAGE_PER_LOCALE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest
@SpringBootTest
class EmailProcessingFlowTests extends BaseIntegrationTest {
    
    @Qualifier(EMAIL_PROCESSING_FLOW_INPUT_CHANNEL_ID)
    @Autowired private PublishSubscribeChannel emailProcessingInputChangel;
    @Autowired private EmailProcessingFlowProperties properties;
    
    @BeforeEach
    void setupGreenMail() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }
    
    @Test
    void testSendMultiLocaleMessagesTypeMerge() throws Exception {
        // given
        properties.setMultiLocalesMergeStrategy(MERGE);
        Message inputMessage = JsonUtils.readObjectFromClassPathResource("messages/templated/teamplate_message_en_de.json", Message.class);
        emailProcessingInputChangel.send(new GenericMessage<>(inputMessage));
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
    
        assertThat(contentAsDocument.body().children().get(0).text(), equalTo("Hallo World"));
        assertThat(contentAsDocument.body().children().get(1).text(), equalTo("John Doe"));
        assertThat(contentAsDocument.body().children().get(2).tagName(), equalTo("br"));
        assertThat(contentAsDocument.body().children().get(3).text(), equalTo("Grues gott"));
        assertThat(contentAsDocument.body().children().get(4).text(), equalTo("John Doe"));
        
        assertMessagesContains(receivedMessages, MailMessageForAssertions.as("john.doe@objectify.sk", "Subject/Subject"));
    }
    
    @Test
    void testSendMultiLocaleMessagesTypeMessagePerLocale() {
        // given
        properties.setMultiLocalesMergeStrategy(MESSAGE_PER_LOCALE);
        Message inputMessage = JsonUtils.readObjectFromClassPathResource("messages/templated/teamplate_message_en_de.json", Message.class);
        emailProcessingInputChangel.send(new GenericMessage<>(inputMessage));
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
        Message inputMessage = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message1.json", Message.class);
        emailProcessingInputChangel.send(new GenericMessage<>(inputMessage));
        // when
        boolean success = greenMail.waitForIncomingEmail(5000L, 1);
        // then
        assertThat(success, equalTo(true));
        
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertThat(receivedMessages.length, equalTo(1));
    }
    
    @RegisterExtension
    protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(
                    GreenMailConfiguration.aConfig()
                            .withUser("no-reply@objectify.sk", "xxx"))
            .withPerMethodLifecycle(true);
    
}