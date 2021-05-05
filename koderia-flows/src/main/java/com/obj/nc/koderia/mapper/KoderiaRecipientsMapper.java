package com.obj.nc.koderia.mapper;

import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.Person;
import com.obj.nc.koderia.domain.recipients.RecipientDto;

@Component
@Log4j2
public class KoderiaRecipientsMapper {

    public MailchimpEndpoint map(RecipientDto recipientDto) {
        if (recipientDto == null) {
            return null;
        }

        Person person = new Person(recipientDto.getFirstName() + " " + recipientDto.getLastName());
        MailchimpEndpoint mailChimpEndpoint = MailchimpEndpoint.createForPerson(recipientDto.getEmail(), person);
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        mailChimpEndpoint.setDeliveryOptions(deliveryOptions);
        return mailChimpEndpoint;
    }

}
