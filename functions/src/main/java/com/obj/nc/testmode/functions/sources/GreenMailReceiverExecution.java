package com.obj.nc.testmode.functions.sources;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.InMemoryStore;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.store.StoredMessage;
import com.obj.nc.domain.Messages;
import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.Content;
import com.obj.nc.domain.message.Email;
import com.obj.nc.testmode.functions.processors.TestModeEmailSenderProperties;
import com.obj.nc.utils.GreenMailManager;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.mail.util.MimeMessageParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Log4j2
@Component
public class GreenMailReceiverExecution implements Supplier<Messages> {

    @Autowired
    private GreenMailManager greenMailManager;

    @Autowired
    private TestModeEmailSenderProperties properties;

    @Override
    public Messages get() {
        List<Message> allMessages = new ArrayList<>();

        try {
            InMemoryStore store = (InMemoryStore) greenMailManager.getGreenMail().getManagers().getImapHostManager().getStore();
            Collection<MailFolder> mailboxes = store.listMailboxes("*");

            for (MailFolder folder : mailboxes) {
                List<Message> folderMessages = folder.getNonDeletedMessages().stream().map(
                        greenMailMessage -> {
                            Message message = convertGreenMailMessageToMessage(greenMailMessage);
                            greenMailMessage.setFlag(Flags.Flag.DELETED, true);
                            return message;
                        }
                ).collect(Collectors.toList());

                allMessages.addAll(folderMessages);
                folder.expunge();
            }

        } catch (FolderException e) {
            throw new RuntimeException(e);
        }

        return new Messages(allMessages);
    }

    private Message convertGreenMailMessageToMessage(StoredMessage message) {
        Message result = Message.createAsEmail();

        try {
            MimeMessage mimeMessage = message.getMimeMessage();
            Email content = result.getContentTyped();
            content.setSubject(mimeMessage.getSubject());

            MimeMessageParser parser = new MimeMessageParser(mimeMessage).parse();
            String originalRecipients = parser.getTo().stream().map(Address::toString).collect(Collectors.joining(","));
            String mimeMessageContent = parser.hasHtmlContent() ? parser.getHtmlContent() : parser.getPlainContent();
            content.setText(originalRecipients + "\n" + mimeMessageContent);

            EmailEndpoint emailEndpoint = new EmailEndpoint();
            emailEndpoint.setEmail(properties.getRecipient());
            result.getBody().setRecievingEndpoints(Collections.singletonList(emailEndpoint));

            DeliveryOptions deliveryOptions = new DeliveryOptions();
            deliveryOptions.setAggregationType(DeliveryOptions.AGGREGATION_TYPE.ONCE_A_DAY);
            result.getBody().setDeliveryOptions(deliveryOptions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

}
