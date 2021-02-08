package com.obj.nc.domain.mapper;

import com.obj.nc.domain.endpoints.DeliveryOptions;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.Person;
import com.obj.nc.domain.koderia.RecipientDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface RecipientMapper {
    @Mappings({
            @Mapping(target = "recipient", source = "recipientDto", qualifiedBy = PersonMapper.class),
            @Mapping(target = "email", source = "recipientDto.email"),
            @Mapping(target = "deliveryOptions", source = "recipientDto", qualifiedBy = DeliveryOptionsMapper.class)
    })
    EmailEndpoint recipientDtoToEmailEndpoint(RecipientDto recipientDto);

    @PersonMapper
    static Person recipientDtoToPerson(RecipientDto recipientDto) {
        return new Person(recipientDto.getFirstName() + " " + recipientDto.getLastName());
    }

    @DeliveryOptionsMapper
    static DeliveryOptions recipientDtoToDeliveryOptions(RecipientDto recipientDto) {
        return new DeliveryOptions();
    }
}
