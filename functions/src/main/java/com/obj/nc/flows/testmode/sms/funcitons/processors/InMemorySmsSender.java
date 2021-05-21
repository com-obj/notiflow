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
public class InMemorySmsSender extends ProcessorFunctionAdapter<Message<SimpleTextContent>,Message<SimpleTextContent>> implements SmsSender  {
	
	private final InMemorySmsSourceSupplier reciever;

	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(Message<SimpleTextContent> payload) {
		if (!(payload.getBody() instanceof SimpleTextContent)) {
			throw new PayloadValidationException("TestModeSmsSender can only process SimpleTextContent content. Was " + payload.getBody() );
		}
		return Optional.empty();
	}
	
	@Override
	protected Message<SimpleTextContent> execute(Message<SimpleTextContent> smsMessage) {
				
		reciever.recieve(smsMessage);

		return smsMessage;
	}


}
