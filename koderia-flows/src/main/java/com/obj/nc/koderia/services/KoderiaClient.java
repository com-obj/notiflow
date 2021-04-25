package com.obj.nc.koderia.services;

import java.util.List;

import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.koderia.dto.RecipientsQueryDto;

public interface KoderiaClient extends RestClient {

    List<RecievingEndpoint> findReceivingEndpoints(RecipientsQueryDto query);

}
