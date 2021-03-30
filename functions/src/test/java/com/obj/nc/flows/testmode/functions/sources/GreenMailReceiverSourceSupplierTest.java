package com.obj.nc.flows.testmode.functions.sources;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import javax.mail.internet.MimeMessage;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.Body;
import com.obj.nc.domain.content.email.AggregatedEmailContent;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.flows.testmode.config.TestModeBeansConfig;
import com.obj.nc.flows.testmode.config.TestModeProperties;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.utils.JsonUtils;

@ActiveProfiles(value = {"test"}, resolver = SystemPropertyActiveProfileResolver.class)
@SpringBootTest(properties = {
		"nc.flows.test-mode.enabled=true", 
		"nc.flows.test-mode.recipients=cuzy@objectify.sk",
		"nc.flows.test-mode.periodInSeconds=64000"}) //Don't poll, I'll make it by my self
@DirtiesContext(classMode = ClassMode.AFTER_CLASS) //Because of correct disposal of green mail used for test mode
class GreenMailReceiverSourceSupplierTest extends BaseIntegrationTest {

	@Qualifier(TestModeBeansConfig.TEST_MODE_GREEN_MAIL_BEAN_NAME)
    @Autowired private GreenMail testModeEmailsReciver;
	@Autowired private TestModeProperties properties;
	@Autowired private EmailSender emailSenderSinkProcessingFunction;
	@Autowired private GreenMailReceiverSourceSupplier greenMailReceiverSourceSupplier;

    @BeforeEach
    void setUp() throws FolderException {
    	testModeEmailsReciver.purgeEmailFromAllMailboxes();
    }

    @Test
    void testRecieveAndConvertMailsFromGreenMail() {
    	//normaly, we would send all test mail to standrdTestGMServer. When testMode profile is activated, aditional testModeEmailsReciver
    	//is created which is a different instannce. In this mode testModeEmailsReciver will catch all emails normaly send to standardTestGMServer
    	//and thus in production will catch all emails send to standard SMTP server configured
    	//PRE-CONDITION
    	Assertions.assertThat(greenMail).isNotEqualTo(testModeEmailsReciver);
    	Assertions.assertThat(greenMail.getSmtp().getPort()).isNotEqualTo(testModeEmailsReciver.getSmtp().getPort());
        // GIVEN
        Message origianlMsgForAggreagtion1 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message1.json", Message.class);
        Message origianlMsgForAggreagtion2 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message2.json", Message.class);
        Message origianlMsgForAggreagtion3 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message3.json", Message.class);

        //WHEN
        emailSenderSinkProcessingFunction.apply(origianlMsgForAggreagtion1);
        emailSenderSinkProcessingFunction.apply(origianlMsgForAggreagtion2);
        emailSenderSinkProcessingFunction.apply(origianlMsgForAggreagtion3);

        //THEN testModeEmailsReciver recieved the message, not the standard greenmail used for test. This proves it has been substituted
        boolean success = testModeEmailsReciver.waitForIncomingEmail(3);
        Assertions.assertThat( success ).isEqualTo( true );
        MimeMessage[] mimeMessages = testModeEmailsReciver.getReceivedMessages();
        Assertions.assertThat( mimeMessages.length ).isEqualTo(3);

        // WHEN
        List<Message> msgsCauthByTestModeGM = greenMailReceiverSourceSupplier.get();
        msgsCauthByTestModeGM.forEach(m-> assertThat(m.getHeader().getEventIds()).contains(UUID.fromString("23e201b5-d7fa-4231-a520-51190b5c50da")));

        EmailContent emailContentFromTMGM1 = msgsCauthByTestModeGM.get(0).getContentTyped();
        checkRecievedMatchOriginal(origianlMsgForAggreagtion1, emailContentFromTMGM1);

        EmailContent emailContentFromTMGM2 = msgsCauthByTestModeGM.get(1).getContentTyped();
        checkRecievedMatchOriginal(origianlMsgForAggreagtion2, emailContentFromTMGM2);


        EmailContent emailContentFromTMGM3 = msgsCauthByTestModeGM.get(2).getContentTyped();
        checkRecievedMatchOriginal(origianlMsgForAggreagtion3, emailContentFromTMGM3);

        Body emailBodyFromTMGM2 = msgsCauthByTestModeGM.get(2).getBody();
        
        assertThat(emailBodyFromTMGM2.getRecievingEndpoints()).hasSize(1);
        String recipient = properties.getRecipients().iterator().next();
        assertThat(((EmailEndpoint) emailBodyFromTMGM2.getRecievingEndpoints().get(0)).getEmail()).isEqualTo(recipient);

        assertThat(emailBodyFromTMGM2.getDeliveryOptions()).isNotNull();
        assertThat((emailBodyFromTMGM2.getDeliveryOptions().getAggregationType())).isEqualTo(DeliveryOptions.AGGREGATION_TYPE.ONCE_A_DAY);
    }

	private void checkRecievedMatchOriginal(Message origianlMsgForAggreagtion, EmailContent emailContentFromTMGM) {
		EmailContent originalContent1 = ((AggregatedEmailContent)origianlMsgForAggreagtion.getContentTyped()).getAggregateContent().get(0);
        String originalReviever1 = ((EmailEndpoint) origianlMsgForAggreagtion.getBody().getRecievingEndpoints().get(0)).getEmail();
        assertThat(emailContentFromTMGM.getSubject())
        	.contains(originalContent1.getSubject());
        assertThat(emailContentFromTMGM.getAttributeValue(GreenMailReceiverSourceSupplier.ORIGINAL_RECIPIENTS_ATTR_NAME).toString())
        	.contains(originalReviever1);
        assertThat(emailContentFromTMGM.getText())
        	.contains(originalContent1.getText());
	}
}