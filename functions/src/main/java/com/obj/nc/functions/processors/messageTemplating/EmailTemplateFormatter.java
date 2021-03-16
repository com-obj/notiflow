package com.obj.nc.functions.processors.messageTemplating;

import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessageContent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.AllArgsConstructor;

@Primary
@Component
@AllArgsConstructor
public class EmailTemplateFormatter extends ProcessorFunctionAdapter<Message, Message> {
	

	@Override
	public Optional<PayloadValidationException> checkPreCondition(Message message) {
		MessageContent content = message.getBody().getMessage();

		if (content ==null ) {
			return Optional.of(new PayloadValidationException("EmailTemplateFormatter cannot format message because its content is null"));
		}

		return Optional.empty();
	}


	@DocumentProcessingInfo("EmailTemplateFormatter")
	@Override
	public Message execute(Message payload) {
		MessageContent content = payload.getBody().getMessage();
		
		
		return payload;
	}


}
