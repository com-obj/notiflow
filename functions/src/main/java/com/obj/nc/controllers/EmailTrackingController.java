package com.obj.nc.controllers;

import com.obj.nc.functions.processors.deliveryInfo.domain.DeliveryInfo;
import com.obj.nc.repositories.DeliveryInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/email-tracking")
public class EmailTrackingController {
    
    @Autowired private DeliveryInfoRepository deliveryRepo;
    
    @GetMapping(value = "/read/{messageId}")
    public String trackMessageRead(@PathVariable(value = "messageId", required = true) String messageId) {
        List<DeliveryInfo> deliveryInfos = deliveryRepo.findByMessageId(UUID.fromString(messageId));
        
        deliveryInfos.stream().peek(deliveryInfo -> deliveryInfo.setStatus(DeliveryInfo.DELIVERY_STATUS.READ))
                .forEach(deliveryInfo -> deliveryRepo.save(deliveryInfo));
        
        return "redirect:/resources/images/px.png";
    }
    
}
