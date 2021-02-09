package com.obj.nc.domain.mapper;

import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.Person;
import com.obj.nc.domain.koderia.RecipientDto;
import org.springframework.stereotype.Component;

@Component
public class RecipientMapperImpl implements RecipientMapper {

    @Override
    public EmailEndpoint map(RecipientDto recipientDto) {
        if (recipientDto == null) {
            return null;
        }

        EmailEndpoint emailEndpoint = new EmailEndpoint();
        emailEndpoint.setEmail(recipientDto.getEmail());

        Person person = new Person(recipientDto.getFirstName() + " " + recipientDto.getLastName());
        emailEndpoint.setRecipient(person);

        DeliveryOptions deliveryOptions = new DeliveryOptions();
        emailEndpoint.setDeliveryOptions(deliveryOptions);

        return emailEndpoint;
    }

}
