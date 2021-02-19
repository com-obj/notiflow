package com.obj.nc.services;

import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.dto.RecipientsQueryDto;

import java.util.List;

public interface KoderiaService extends RestClient {

    List<RecievingEndpoint> findReceivingEndpoints(RecipientsQueryDto query);

}
