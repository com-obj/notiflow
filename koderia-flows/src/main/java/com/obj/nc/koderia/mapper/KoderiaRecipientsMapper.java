package com.obj.nc.koderia.mapper;

import org.springframework.stereotype.Component;

import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.Person;
import com.obj.nc.koderia.domain.recipients.RecipientDto;

@Component
public class KoderiaRecipientsMapper implements RecipientMapper {

    @Override
    public EmailEndpoint map(RecipientDto recipientDto) {
        if (recipientDto == null) {
            return null;
        }

        EmailEndpoint emailEndpoint = new EmailEndpoint(recipientDto.getEmail());

        Person person = new Person(recipientDto.getFirstName() + " " + recipientDto.getLastName());
        emailEndpoint.setRecipient(person);

        DeliveryOptions deliveryOptions = new DeliveryOptions();
        emailEndpoint.setDeliveryOptions(deliveryOptions);

        return emailEndpoint;
    }

}
