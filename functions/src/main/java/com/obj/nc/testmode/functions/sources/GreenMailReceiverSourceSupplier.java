package com.obj.nc.testmode.functions.sources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.internet.MimeMessage;

import org.apache.commons.mail.util.MimeMessageParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.InMemoryStore;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.store.StoredMessage;
import com.icegreen.greenmail.util.GreenMail;
import com.obj.nc.domain.Messages;
import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Email;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sources.SourceSupplierAdapter;
import com.obj.nc.testmode.config.TestModeBeansConfig;
import com.obj.nc.testmode.config.TestModeProperties;

@Component
@ConditionalOnProperty(value = "nc.flows.test-mode.enabled", havingValue = "true")
public class GreenMailReceiverSourceSupplier extends SourceSupplierAdapter<Messages> {

    @Qualifier(TestModeBeansConfig.TEST_MODE_GREEN_MAIL_BEAN_NAME)
    @Autowired private GreenMail gm;

    @Autowired private TestModeProperties properties;

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(Messages payload) {
		//toto neni na stav, kedy by bolo treba hadzat vynimku.. ked je source prazdy, tak je prazdny. nic sa nedeje
//        if (payload.getMessages().isEmpty()) {
//            return Optional.of(new PayloadValidationException("There are no messages to supply"));
//        }

		if (properties.getRecipients()==null) {
			return Optional.of(new PayloadValidationException("No recipients defined for nc.flows.test-mode.recipients. Test mode sender cannot send aggregated email"));
		}
		
        return Optional.empty();
	}

	@Override
	protected Messages execute() {
        List<Message> allMessages = new ArrayList<>();

        try {
            InMemoryStore store = (InMemoryStore) gm.getManagers().getImapHostManager().getStore();
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
        
        if (allMessages.size()==0) {
        	return null; //terminates further flow processing
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

            List<RecievingEndpoint> emailEndpoints = properties.getRecipients().stream().map(rec-> new EmailEndpoint(rec)).collect(Collectors.toList());
            result.getBody().setRecievingEndpoints(emailEndpoints);

            DeliveryOptions deliveryOptions = new DeliveryOptions();
            deliveryOptions.setAggregationType(DeliveryOptions.AGGREGATION_TYPE.ONCE_A_DAY);
            result.getBody().setDeliveryOptions(deliveryOptions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

}
