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

import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowConfig.EMAIL_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ActiveProfiles(value = { "test" }, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest
@SpringBootTest(properties = {
        "nc.flows.email-processing.multi-locales-merge-strategy=MERGE"})
class EmailProcessingFlowTest extends BaseIntegrationTest {
    
    @Qualifier(EMAIL_PROCESSING_FLOW_INPUT_CHANNEL_ID)
    @Autowired private PublishSubscribeChannel emailProcessingInputChangel;
    
    @BeforeEach
    void setupGreenMail() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }
    
    @Test
    void testEmailProcessingFlowMultiLocaleMerge() throws Exception {
        // given
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
    
        assertThat(contentAsDocument.body().children().eachText(), containsInRelativeOrder("Hallo World", "John Doe", "Grues gott", "John Doe"));
        assertMessagesContains(receivedMessages, MailMessageForAssertions.as("john.doe@objectify.sk", "Subject/Subject"));
    }
    
    @RegisterExtension
    protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(
                    GreenMailConfiguration.aConfig()
                            .withUser("no-reply@objectify.sk", "xxx"))
            .withPerMethodLifecycle(true);
    
}