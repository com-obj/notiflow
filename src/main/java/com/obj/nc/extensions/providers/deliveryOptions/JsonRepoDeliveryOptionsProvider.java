package com.obj.nc.extensions.providers.deliveryOptions;

import com.obj.nc.domain.deliveryOptions.EndpointDeliveryOptionsConfig;
import com.obj.nc.domain.deliveryOptions.SpamPreventionOption;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.SlackEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.endpoints.TeamsEndpoint;
import com.obj.nc.domain.endpoints.push.PushEndpoint;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

@Component
public class JsonRepoDeliveryOptionsProvider implements DeliveryOptionsProvider {

    private SpamPreventionConfigProperties config;
    
    private DeliveryOptionsJsonRepository repo;

    public JsonRepoDeliveryOptionsProvider(SpamPreventionConfigProperties config) {
        this.config = config;

        if (this.config.getJsonRepoPathAndFileName()==null) {
            repo = new DeliveryOptionsJsonRepository();
        } else {
            repo = DeliveryOptionsJsonRepository.loadRepository(this.config.getJsonRepoPathAndFileName());
        }
    }

    @Override
    public EndpointDeliveryOptionsConfig findDeliveryOptions(ReceivingEndpoint forEndpoint) {
        EndpointDeliveryOptionsConfig deliveryOptions = new EndpointDeliveryOptionsConfig();

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
    
}
