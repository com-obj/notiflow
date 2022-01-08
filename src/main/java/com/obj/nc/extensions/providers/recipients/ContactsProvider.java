package com.obj.nc.extensions.providers.recipients;

import java.util.List;
import java.util.UUID;

import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.recipients.Recipient;

public interface ContactsProvider {
    
    public List<ReceivingEndpoint> findEndpoints(UUID forRecipient);

    public ReceivingEndpoint findEndpoint(String forEndpointId);

    public Recipient findRecipients(String forEndpointId);

    public List<Recipient> findRecipients(UUID ... recipientsIds);

    public List<Recipient> findRecipientsByName(String ... recipientNames);

}
