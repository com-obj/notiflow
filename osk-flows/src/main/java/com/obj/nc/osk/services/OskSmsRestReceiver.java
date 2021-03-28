package com.obj.nc.osk.services;

import com.obj.nc.osk.dto.OskSendSmsRequestDto;
import com.obj.nc.osk.dto.OskSendSmsResponseDto;
import com.obj.nc.osk.dto.SendSmsResourceReferenceDto;
import com.obj.nc.services.BaseRestReceiver;
import org.springframework.stereotype.Service;

@Service
public class OskSmsRestReceiver extends BaseRestReceiver<OskSendSmsRequestDto, OskSendSmsResponseDto> {
    
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
