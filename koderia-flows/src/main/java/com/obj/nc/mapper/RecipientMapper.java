package com.obj.nc.mapper;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.dto.RecipientDto;

public interface RecipientMapper {

    EmailEndpoint map(RecipientDto recipientDto);

}
