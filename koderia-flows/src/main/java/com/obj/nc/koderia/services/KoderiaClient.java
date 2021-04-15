package com.obj.nc.koderia.services;

import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.koderia.dto.koderia.recipients.RecipientsQueryDto;

import java.util.List;

public interface KoderiaClient extends RestClient {

    List<RecievingEndpoint> findReceivingEndpoints(RecipientsQueryDto query);

}
