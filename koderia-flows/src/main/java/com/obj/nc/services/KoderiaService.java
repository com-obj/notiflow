package com.obj.nc.services;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.koderia.RecipientsQueryDto;

import java.util.List;

public interface KoderiaService {

    List<EmailEndpoint> findEmailEndpoints(RecipientsQueryDto query);

}
