package com.obj.nc.flows.testmode.sms.funcitons.processors;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.flows.testmode.sms.funcitons.sources.InMemorySmsSourceSupplier;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.senders.SmsSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@DocumentProcessingInfo("TestSMSSender")
public class InMemorySmsSender extends ProcessorFunctionAdapter<Message,Message> implements SmsSender  {
	
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
		
		reciever.recieve(messageContent);

		return smsMessage;
	}

	private Message convertAggregatedIfNeeded(Message smsMessage) {
		SimpleTextContent content = smsMessage.getContentTyped();
		smsMessage.getBody().setMessage(content);
		return smsMessage;
	}

}
