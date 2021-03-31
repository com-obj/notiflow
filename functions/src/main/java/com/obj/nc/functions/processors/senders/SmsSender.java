package com.obj.nc.functions.processors.senders;

import java.util.Optional;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.services.SmsSenderExcecution;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SmsSender extends ProcessorFunctionAdapter<Message, Message> {

    public static final String SEND_SMS_RESPONSE_ATTRIBUTE = "sendSmsResponse";
    
    private final SmsSenderExcecution<?> smsSenderExcecution;

    @Override
    protected Optional<PayloadValidationException> checkPreCondition(Message payload) {
        if (payload == null) {
            return Optional.of(new PayloadValidationException("Message must not be null"));
        }

        if (payload.getBody().getRecievingEndpoints().stream().anyMatch(endpoint -> !(endpoint instanceof SmsEndpoint))) {
            return Optional.of(new PayloadValidationException(String.format("Sms sender can only send message to endpoint of type %s", SmsEndpoint.JSON_TYPE_IDENTIFIER)));
        }

        if (!(payload.getBody().getMessage() instanceof SimpleTextContent)) {
            return Optional.of(new PayloadValidationException(String.format("Sms sender can only send message with content of type %s", SimpleTextContent.JSON_TYPE_IDENTIFIER)));
        }

        return Optional.empty();
    }

    @Override
    @DocumentProcessingInfo("SmsSender")
    protected Message execute(Message payload) {
        Object sendSmsResponse = smsSenderExcecution.apply(payload);
        payload.getBody().setAttributeValue(SEND_SMS_RESPONSE_ATTRIBUTE, sendSmsResponse);
        return payload;
    }

}
