package com.obj.nc.flows.testmode.sms.funcitons.processors;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.flows.testmode.sms.funcitons.sources.InMemorySmsSourceSupplier;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.senders.SmsSender;

import lombok.RequiredArgsConstructor;

@Component
@ConditionalOnMissingBean(type = "SmsSender")
@RequiredArgsConstructor
@DocumentProcessingInfo("TestSMSSender")
public class InMemorySmsSender extends ProcessorFunctionAdapter<Message,Message> implements SmsSender  {
	
	public static final String INVALID_GSM_0038_CHARACTERS = "[^A-Za-z0-9 \\r\\n@£$¥èéùìòÇØøÅå\\x{0394}_\\x{03A6}\\x{0393}\\x{039B}\\x{03A9}\\x{03A0}\\x{03A8}\\x{03A3}\\x{0398}\\x{039E}ÆæßÉ!\\\"#$%&\\'\\(\\)*+,\\-.\\/:;<=>;?¡ÄÖÑÜ§¿äöñüà^{}\\[\\~\\]\\|\\x{20AC}\\\\]";
	
	private final InMemorySmsSourceSupplier reciever;

	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(Message payload) {
		if (!(payload.getBody().getMessage() instanceof SimpleTextContent)) {
			throw new PayloadValidationException("TestModeSmsSender can only process SimpleTextContent content. Was " + payload.getBody().getMessage() );
		}
		return Optional.empty();
	}
	
	@Override
	protected Message execute(Message smsMessage) {
		
		Message messageContent = convertAggregatedIfNeeded(smsMessage);
		
		SimpleTextContent smsContent = messageContent.getContentTyped();
		String text = formatGSMAlphabetForGAP(smsContent.getText());
		smsContent.setText(text);
		
		reciever.recieve(messageContent);

		return smsMessage;
	}

	private Message convertAggregatedIfNeeded(Message smsMessage) {
		SimpleTextContent content = smsMessage.getContentTyped();
		smsMessage.getBody().setMessage(content);
		return smsMessage;
	}
	
	private String formatGSMAlphabetForGAP(String text) {
		if (text == null) {
			return null;
		}
		
		return text.replaceAll(INVALID_GSM_0038_CHARACTERS, "");
	}

}
