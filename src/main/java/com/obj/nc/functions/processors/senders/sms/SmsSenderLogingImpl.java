package com.obj.nc.functions.processors.senders.sms;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.SmsMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.senders.SmsSender;
import lombok.extern.java.Log;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Validated
@DocumentProcessingInfo("LOG_SMSSender")
@Log
@ConditionalOnProperty(prefix = "nc.sms", name = "logging", matchIfMissing = true)	
@Component
public class SmsSenderLogingImpl extends ProcessorFunctionAdapter<SmsMessage, SmsMessage> implements SmsSender {


    public SmsSenderLogingImpl() {
    }
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(SmsMessage payload) {
        if (payload == null) {
            return Optional.of(new PayloadValidationException("Message must not be null"));
        }

        if (payload.getReceivingEndpoints().stream().anyMatch(endpoint -> !(endpoint instanceof SmsEndpoint))) {
            return Optional.of(new PayloadValidationException(String.format("Sms sender can only send message to endpoint of type %s", SmsEndpoint.JSON_TYPE_IDENTIFIER)));
        }

        if (!(payload.getBody() instanceof SimpleTextContent)) {
            return Optional.of(new PayloadValidationException(String.format("Sms sender can only send message with content of type %s", payload.getBody().getClass())));
        }

        return Optional.empty();
    }
    
	@Override
	protected SmsMessage execute(SmsMessage payload) {
        log.info("############################### SMS SEND TO: "+ payload.getReceivingEndpoints().iterator().next().getPhone() +" ############################");
        log.info(payload.getBody().getText());
        log.info("##################################################################################");
        return payload;
	}





}
