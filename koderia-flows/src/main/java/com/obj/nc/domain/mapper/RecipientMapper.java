package com.obj.nc.domain.mapper;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.koderia.RecipientDto;

public interface RecipientMapper {

    EmailEndpoint map(RecipientDto recipientDto);

}
