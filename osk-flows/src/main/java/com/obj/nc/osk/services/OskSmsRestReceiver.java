package com.obj.nc.osk.services;

import com.obj.nc.osk.dto.OskSendSmsRequestDto;
import com.obj.nc.osk.dto.OskSendSmsResponseDto;
import com.obj.nc.osk.dto.SendSmsResourceReferenceDto;
import com.obj.nc.services.BaseRestReceiver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "nc.flows.test-mode.enabled", havingValue = "true")
public class OskSmsRestReceiver extends BaseRestReceiver<OskSendSmsRequestDto, OskSendSmsResponseDto> {
    
    @Override
    protected OskSendSmsResponseDto createDummyResponse() {
        OskSendSmsResponseDto response = new OskSendSmsResponseDto();
        SendSmsResourceReferenceDto resourceReference = new SendSmsResourceReferenceDto();
        resourceReference.setResourceURL("/3333/requests/+420047013370-SUCCESS-0000000000000000000000000000000000000478#");
        response.setResourceReference(resourceReference);
        return response;
    }
    
}
