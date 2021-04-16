package com.obj.nc.flows.testmode.sms.funcitons.processors;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.flows.testmode.sms.funcitons.sources.InMemorySmsSourceSupplier;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.senders.SmsSender;

@DocumentProcessingInfo("TestSMSSender")
public class TestModeSmsSender extends ProcessorFunctionAdapter<Message,Message> implements SmsSender  {
	
	@Autowired
	private InMemorySmsSourceSupplier reciever;

	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(Message payload) {
		if (!(payload.getBody().getMessage() instanceof SimpleTextContent)) {
			throw new PayloadValidationException("TestModeSmsSender can only process SimpleTextContent content. Was " + payload.getBody().getMessage() );
		}
		return Optional.empty();
	}
	
	@Override
	protected Message execute(Message smsMessage) {
		reciever.recieve(smsMessage);

		return smsMessage;
	}

}
