package com.obj.nc.osk.functions.senders;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.SimpleText;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.osk.dto.SendSmsRequestDto;
import com.obj.nc.osk.dto.SendSmsResponseDto;
import com.obj.nc.osk.service.SmsClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.obj.nc.osk.service.SmsRestClientConstants.SEND_SMS_REQUEST_ATTRIBUTE;
import static com.obj.nc.osk.service.SmsRestClientConstants.SEND_SMS_RESPONSE_ATTRIBUTE;

@Component
@AllArgsConstructor
public class SmsSender extends ProcessorFunctionAdapter<Message, Message> {

    private final SmsSenderConfigProperties properties;
    private final SmsClient smsClient;

    @Override
    protected Optional<PayloadValidationException> checkPreCondition(Message payload) {
        if (payload == null) {
            return Optional.of(new PayloadValidationException("Message must not be null"));
        }

        if (payload.getBody().getRecievingEndpoints().stream().anyMatch(endpoint -> !SmsEndpoint.JSON_TYPE_IDENTIFIER.equals(endpoint.getEndpointTypeName()))) {
            return Optional.of(new PayloadValidationException(String.format("Sms sender can only send message to endpoint of type %s", SmsEndpoint.JSON_TYPE_IDENTIFIER)));
        }

        if (!SimpleText.JSON_TYPE_IDENTIFIER.equals(payload.getBody().getMessage().getContentTypeName())) {
            return Optional.of(new PayloadValidationException(String.format("Sms sender can only send message with content of type %s", SimpleText.JSON_TYPE_IDENTIFIER)));
        }

        return Optional.empty();
    }

    @Override
    @DocumentProcessingInfo("SmsSender")
    protected Message execute(Message payload) {
        SendSmsRequestDto sendSmsRequest = SendSmsRequestDto.from(payload, properties);
        payload.getBody().setAttributeValue(SEND_SMS_REQUEST_ATTRIBUTE, sendSmsRequest);

        SendSmsResponseDto sendSmsResponse = smsClient.sendSms(sendSmsRequest);

        payload.getBody().setAttributeValue(SEND_SMS_RESPONSE_ATTRIBUTE, sendSmsResponse);
        return payload;
    }

}
