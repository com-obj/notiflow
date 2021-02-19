package com.obj.nc.services;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.dto.RecipientsQueryDto;

import java.util.List;

public interface KoderiaService extends RestClient {

    List<EmailEndpoint> findEmailEndpoints(RecipientsQueryDto query);

}
