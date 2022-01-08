package com.obj.nc.extensions.providers.deliveryOptions;

import com.obj.nc.domain.deliveryOptions.EndpointDeliveryOptions;
import com.obj.nc.domain.deliveryOptions.RecipientDeliveryOptions;
import com.obj.nc.domain.deliveryOptions.SpamPreventionOption;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.SlackEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.endpoints.TeamsEndpoint;
import com.obj.nc.domain.endpoints.push.PushEndpoint;
import com.obj.nc.domain.recipients.Recipient;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

/**
 * Default strategy for delivery options resolution.
 *      * delivery options are stored in plain json file
 *      * delivery options are stored globally and for each endpoint
 *      * delivery options resolution favors specific endpoint configuration before global configuration
 */
@Component
public class JsonStoreDeliveryOptionsProvider implements DeliveryOptionsProvider {

    private DeliveryOptionsConfigProperties config;
    
    private DeliveryOptionsJsonStore repo;

    public JsonStoreDeliveryOptionsProvider(DeliveryOptionsConfigProperties config) {
        this.config = config;

        if (this.config.getJsonStorePathAndFileName()==null) {
            repo = new DeliveryOptionsJsonStore();
        } else {
            repo = DeliveryOptionsJsonStore.loadRepository(this.config.getJsonStorePathAndFileName());
        }
    }

    @Override
    public EndpointDeliveryOptions findDeliveryOptions(ReceivingEndpoint forEndpoint) {
        EndpointDeliveryOptions deliveryOptions = new EndpointDeliveryOptions();

        SpamPreventionOption endpointConfig = findEndpointSpamPreventionConfig(forEndpoint);
        if (endpointConfig != null) {
            deliveryOptions.setSpamPrevention(endpointConfig);

            return deliveryOptions;
        }

        SpamPreventionOption globalConfig = findDefaultGlobalSpamPreventionConfig(forEndpoint.getClass());
        deliveryOptions.setSpamPrevention(globalConfig);
        
        return deliveryOptions;
    }

    protected SpamPreventionOption findEndpointSpamPreventionConfig(ReceivingEndpoint forEndpoint) {
        SpamPreventionOption config;

        if (forEndpoint instanceof EmailEndpoint) {
            config = repo.getEmail().get(forEndpoint.getEndpointId());
        } else if (forEndpoint instanceof PushEndpoint) {
            config =  repo.getPush().get(forEndpoint.getEndpointId());
        } else if (forEndpoint instanceof SmsEndpoint) {
            config =  repo.getSms().get(forEndpoint.getEndpointId());
        } else if (forEndpoint instanceof SlackEndpoint) {
            config =  repo.getSlack().get(forEndpoint.getEndpointId());
        } else if (forEndpoint instanceof TeamsEndpoint) {
            config =  repo.getTeams().get(forEndpoint.getEndpointId());
        }
        else if (forEndpoint instanceof MailchimpEndpoint) {
            config =  repo.getMailChimp().get(forEndpoint.getEndpointId());
        } else {
            throw new NotImplementedException("No spam preventions could be found for " + forEndpoint.getClass());
        }

        SpamPreventionOptionsValidator.validate(config);
        
        return config;
    }

    protected SpamPreventionOption findDefaultGlobalSpamPreventionConfig(Class<? extends ReceivingEndpoint> forEndpointType) {
        SpamPreventionOption config;

        if (forEndpointType.equals(EmailEndpoint.class)) {
            config = repo.getEmailGlobal();
        } else if (forEndpointType.equals(PushEndpoint.class)) {
            config =  repo.getPushGlobal();
        } else if (forEndpointType.equals(SmsEndpoint.class)) {
            config =  repo.getSmsGlobal();
        } else if (forEndpointType.equals(SlackEndpoint.class)) {
            config =  repo.getSlackGlobal();
        } else if (forEndpointType.equals(TeamsEndpoint.class)) {
            config =  repo.getTeamsGlobal();
        } else if (forEndpointType.equals(MailchimpEndpoint.class)) {
            config =  repo.getMailChimpGlobal();
        } else {
            throw new NotImplementedException("No spam preventions could be found for " + forEndpointType);
        }

        SpamPreventionOptionsValidator.validate(config);
        
        return config;
    }

    @Override
    public RecipientDeliveryOptions findDeliveryOptions(Recipient forRecipient) {
        throw new NotImplementedException();
    }
    
}
