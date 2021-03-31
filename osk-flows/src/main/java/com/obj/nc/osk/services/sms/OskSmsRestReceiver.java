package com.obj.nc.osk.services.sms;

import com.obj.nc.osk.services.sms.dtos.OskSendSmsRequestDto;
import com.obj.nc.osk.services.sms.dtos.OskSendSmsResponseDto;
import com.obj.nc.osk.services.sms.dtos.SendSmsResourceReferenceDto;
import com.obj.nc.services.BaseTestModeSmsReceiver;
import org.springframework.stereotype.Service;

@Service
public class OskSmsRestReceiver extends BaseTestModeSmsReceiver<OskSendSmsRequestDto, OskSendSmsResponseDto> {
    
    @Override
    protected OskSendSmsResponseDto createResponse(OskSendSmsRequestDto request) {
        OskSendSmsResponseDto response = new OskSendSmsResponseDto();
        SendSmsResourceReferenceDto resourceReference = new SendSmsResourceReferenceDto();
        String resourceUrl = "/"
                .concat(request.getSenderAddress())
                .concat("/requests/")
                .concat(request.getAddress().get(0))
                .concat("-SUCCESS-0000000000000000000000000000000000000478#");
        resourceReference.setResourceURL(resourceUrl);
        response.setResourceReference(resourceReference);
        return response;
    }
    
}
