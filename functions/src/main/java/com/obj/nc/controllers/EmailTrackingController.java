package com.obj.nc.controllers;

import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.repositories.DeliveryInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/email-tracking")
public class EmailTrackingController {
    
    private final DeliveryInfoRepository deliveryRepo;
    
    @GetMapping(value = "/read/{messageId}")
    public String trackMessageRead(@PathVariable(value = "messageId", required = true) String messageId) {
        List<DeliveryInfo> deliveryInfos = deliveryRepo.findByMessageId(UUID.fromString(messageId));
        
        deliveryInfos.forEach(deliveryInfo ->  {
            deliveryInfo.setStatus(DeliveryInfo.DELIVERY_STATUS.READ);
            deliveryRepo.save(deliveryInfo);
        });
        
        return "redirect:/resources/images/px.png";
    }
    
}
