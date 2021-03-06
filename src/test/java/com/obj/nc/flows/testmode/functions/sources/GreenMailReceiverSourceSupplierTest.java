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

package com.obj.nc.flows.testmode.functions.sources;


import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.flows.testmode.TestModeProperties;
import com.obj.nc.flows.testmode.email.config.TestModeEmailsBeansConfig;
import com.obj.nc.flows.testmode.email.functions.sources.GreenMailReceiverSourceSupplier;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.internet.MimeMessage;

import java.util.List;
import java.util.UUID;

import static com.obj.nc.flows.testmode.email.config.TestModeEmailsFlowConfig.TEST_MODE_GREEN_MAIL_SOURCE_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles(value = {"test"}, resolver = SystemPropertyActiveProfileResolver.class)
@SpringIntegrationTest(noAutoStartup = TEST_MODE_GREEN_MAIL_SOURCE_BEAN_NAME)
@SpringBootTest(properties = {
		"nc.flows.test-mode.enabled=true", 
		"nc.flows.test-mode.recipients=cuzy@objectify.sk"})
@DirtiesContext(classMode =ClassMode.AFTER_CLASS) //need to dispose @Qualifier(TestModeEmailsBeansConfig.TEST_MODE_GREEN_MAIL_BEAN_NAME) testModeEmailsReciver
@Tag("test-mode")
public class GreenMailReceiverSourceSupplierTest extends BaseIntegrationTest {

	@Qualifier(TestModeEmailsBeansConfig.TEST_MODE_GREEN_MAIL_BEAN_NAME)
    @Autowired private GreenMail testModeGreenMail;
	@Autowired private TestModeProperties properties;
	@Autowired private EmailSender emailSenderSinkProcessingFunction;
	@Autowired private GreenMailReceiverSourceSupplier greenMailReceiverSourceSupplier;
	@Autowired private GenericEventRepository genericEventRepository;
	
    @RegisterExtension
    protected static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
      	.withConfiguration(
      			GreenMailConfiguration.aConfig()
      			.withUser("no-reply@objectify.sk", "xxx"))
      	.withPerMethodLifecycle(true);

    @BeforeEach
    void setUp() throws FolderException {
    	testModeGreenMail.purgeEmailFromAllMailboxes();
    	greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    void testRecieveAndConvertMailsFromGreenMail() {
    	//normaly, we would send all test mail to standrdTestGMServer. When testMode profile is activated, aditional testModeEmailsReciver
    	//is created which is a different instannce. In this mode testModeEmailsReciver will catch all emails normaly send to standardTestGMServer
    	//and thus in production will catch all emails send to standard SMTP server configured
    	//PRE-CONDITION
    	Assertions.assertThat(greenMail).isNotEqualTo(testModeGreenMail);
    	Assertions.assertThat(greenMail.getSmtp().getPort()).isNotEqualTo(testModeGreenMail.getSmtp().getPort());
        // GIVEN
    	EmailMessage origianlMsgForAggreagtion1 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message1.json", EmailMessage.class);
    	EmailMessage origianlMsgForAggreagtion2 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message2.json", EmailMessage.class);
    	EmailMessage origianlMsgForAggreagtion3 = JsonUtils.readObjectFromClassPathResource("messages/testmode/aggregate_input_message3.json", EmailMessage.class);
    
    	// persist event for DB references
        GenericEvent genericEvent = GenericEvent.builder()
                .id(UUID.fromString("23e201b5-d7fa-4231-a520-51190b5c50da"))
                .payloadJson(JsonUtils.readJsonNodeFromJSONString(""))
                .build();
        genericEventRepository.save(genericEvent);

        //WHEN
        emailSenderSinkProcessingFunction.apply(origianlMsgForAggreagtion1);
        emailSenderSinkProcessingFunction.apply(origianlMsgForAggreagtion2);
        emailSenderSinkProcessingFunction.apply(origianlMsgForAggreagtion3);

        //THEN testModeEmailsReciver recieved the message, not the standard greenmail used for test. This proves it has been substituted
        boolean success = testModeGreenMail.waitForIncomingEmail(3);
        Assertions.assertThat( success ).isEqualTo( true );
        MimeMessage[] mimeMessages = testModeGreenMail.getReceivedMessages();
        Assertions.assertThat( mimeMessages.length ).isEqualTo(3);

        // WHEN
        List<EmailMessage> msgsCauthByTestModeGM = greenMailReceiverSourceSupplier.get();

        EmailContent emailContentFromTMGM1 = msgsCauthByTestModeGM.get(0).getBody();
        checkRecievedMatchOriginal(origianlMsgForAggreagtion1, emailContentFromTMGM1);

        EmailContent emailContentFromTMGM2 = msgsCauthByTestModeGM.get(1).getBody();
        checkRecievedMatchOriginal(origianlMsgForAggreagtion2, emailContentFromTMGM2);


        EmailContent emailContentFromTMGM3 = msgsCauthByTestModeGM.get(2).getBody();
        checkRecievedMatchOriginal(origianlMsgForAggreagtion3, emailContentFromTMGM3);

        EmailMessage emailBodyFromTMGM2 = msgsCauthByTestModeGM.get(2);
        
        assertThat(emailBodyFromTMGM2.getReceivingEndpoints()).hasSize(1);
        String recipient = properties.getRecipients().iterator().next();
        assertThat(((EmailEndpoint) emailBodyFromTMGM2.getReceivingEndpoints().get(0)).getEmail()).isEqualTo(recipient);
    }

	private void checkRecievedMatchOriginal(EmailMessage origianlMsgForAggreagtion, EmailContent emailContentFromTMGM) {
		EmailContent originalContent1 = origianlMsgForAggreagtion.getBody();
        String originalReviever1 = ((EmailEndpoint) origianlMsgForAggreagtion.getReceivingEndpoints().get(0)).getEmail();
        assertThat(emailContentFromTMGM.getSubject())
        	.contains(originalContent1.getSubject());
        assertThat(emailContentFromTMGM.getAttributeValue(GreenMailReceiverSourceSupplier.ORIGINAL_RECIPIENTS_EMAIL_ATTR_NAME).toString())
        	.contains(originalReviever1);
        assertThat(emailContentFromTMGM.getText())
        	.contains(originalContent1.getText());
	}
}