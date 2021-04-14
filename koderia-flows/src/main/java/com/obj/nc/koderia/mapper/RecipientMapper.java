package com.obj.nc.koderia.mapper;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.koderia.dto.koderia.data.RecipientDto;

public interface RecipientMapper {

    EmailEndpoint map(RecipientDto recipientDto);

}
