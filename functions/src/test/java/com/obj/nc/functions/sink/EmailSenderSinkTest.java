package com.obj.nc.functions.sink;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.obj.nc.BaseIntegrationTest;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.ProcessingInfo;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.senders.EmailSenderSinkExecution;
import com.obj.nc.functions.processors.senders.EmailSenderSinkProcessingFunction;
import com.obj.nc.utils.GreenMailManager;
import com.obj.nc.utils.JsonUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.UUID;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
class EmailSenderSinkTest extends BaseIntegrationTest {

    @Autowired
    private GreenMailManager greenMailManager;

    @Autowired
    private EmailSenderSinkProcessingFunction functionSend;

    @Autowired
    private EmailSenderSinkExecution.EmailSenderSinkProperties emailFromSetting;

    @BeforeEach
    void cleanGreenMailMailBoxes() throws FolderException {
        greenMailManager.getGreenMail().purgeEmailFromAllMailboxes();
    }

    @Test
    void sendSingleMail() throws MessagingException, IOException {
        //GIVEN
        String INPUT_JSON_FILE = "messages/email_message.json";
        Message message = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);
        UUID originalProcessingId = message.getProcessingInfo().getProcessingId();

        //WHEN
        Message result = functionSend.apply(message);

        //THEN
        GreenMail gm = greenMailManager.getGreenMail();
        MimeMessage[] messages = gm.getReceivedMessages();
        Assertions.assertThat( messages.length ).isEqualTo(1);
        Assertions.assertThat( messages[0].getSubject() ).isEqualTo("Subject");
        Assertions.assertThat( messages[0].getFrom()[0] ).extracting("address").isEqualTo(emailFromSetting.getFrom());

        //THEN check processing info

        ProcessingInfo processingInfo = result.getProcessingInfo();
        Assertions.assertThat(processingInfo.getStepName()).isEqualTo("SendEmail");
        Assertions.assertThat(processingInfo.getStepIndex()).isEqualTo(4);
        Assertions.assertThat(processingInfo.getPrevProcessingId()).isEqualTo(originalProcessingId);
        Assertions.assertThat(processingInfo.getTimeStampStart()).isBeforeOrEqualTo(processingInfo.getTimeStampFinish());
    }

    @Test
    void sendMailToManyRecipients() {
        //GIVEN
        String INPUT_JSON_FILE = "messages/e_email_message_2_many.json";
        Message message = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);

        //WHEN -THEN
        Assertions.assertThatThrownBy(() -> {
            functionSend.apply(message);
        })
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Email sender can send to only one recipient. Found more: ");
    }

    @Test
    void sendEmailToNonEmailEnpoint() {
        //GIVEN
        String INPUT_JSON_FILE = "messages/e_email_message_2_push_endpoint.json";
        Message message = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);

        //WHEN -THEN
        Assertions.assertThatThrownBy(() -> {
            functionSend.apply(message);
        })
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Email sender can send to Email endpoints only. Found ");
    }

    @Test
    void sendMailWithAttachments() {
        //GIVEN
        String INPUT_JSON_FILE = "messages/email_message_attachments.json";
        Message inputMessage = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);

        inputMessage.getBody().getMessage().getContent().getAttachments().forEach(attachement -> {
            try {
                attachement.setFileURI(new ClassPathResource(attachement.getFileURI().getPath()).getURI());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        //WHEN
        Message outputMessage = functionSend.apply(inputMessage);

        //THEN
        GreenMail gm = greenMailManager.getGreenMail();
        MimeMessage message = gm.getReceivedMessages()[0];
        String body = GreenMailUtil.getBody(message);

        Assertions.assertThat(body).contains("name=test1.txt", "attachment test1", "name=test2.txt", "attachment test2");
    }

    @Test
    void sendAggregateMessage() {
        //GIVEN
        String INPUT_JSON_FILE = "messages/aggregate/aggregate_output_message.json";
        Message inputMessage = JsonUtils.readObjectFromClassPathResource(INPUT_JSON_FILE, Message.class);

        //WHEN
        Message outputMessage = functionSend.apply(inputMessage);

        //THEN
        GreenMail gm = greenMailManager.getGreenMail();
        MimeMessage message = gm.getReceivedMessages()[0];
        String msg = GreenMailUtil.getWholeMessage(message);


        outputMessage.getBody().getMessage().getAggregateContent()
                .forEach(messageContent -> {
                    Assertions.assertThat(msg).contains(messageContent.getSubject());
                    Assertions.assertThat(msg).contains(messageContent.getText());
                    messageContent.getAttachments()
                            .forEach(attachment -> Assertions.assertThat(msg).contains(attachment.getName()));
                });
    }
}
