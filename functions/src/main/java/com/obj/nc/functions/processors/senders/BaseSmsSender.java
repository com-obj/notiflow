package com.obj.nc.functions.processors.senders;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.SimpleTextContent;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.services.SmsClient;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public abstract class BaseSmsSender<REQUEST_T, RESPONSE_T> extends ProcessorFunctionAdapter<Message, Message> {

    public static final String SEND_SMS_RESPONSE_ATTRIBUTE = "sendSmsResponse";
    
    private final SmsClient<REQUEST_T, RESPONSE_T> smsClient;

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
        REQUEST_T sendSmsRequest = smsClient.convertMessageToRequest(payload);
        RESPONSE_T sendSmsResponse = smsClient.sendRequest(sendSmsRequest);
        payload.getBody().setAttributeValue(SEND_SMS_RESPONSE_ATTRIBUTE, sendSmsResponse);
        return payload;
    }

}
