package com.obj.nc.flows.dataSources.config;

import com.obj.nc.converterExtensions.genericData.GenericData2NotificationConverterExtension;
import com.obj.nc.domain.IsNotification;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.domain.dataObject.GenericData;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.message.EmailMessageTemplated;
import com.obj.nc.exceptions.PayloadValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class GenericDataToNotificationConverter implements GenericData2NotificationConverterExtension {
	
	private final LicenseAgreementProperties properties;
	
	@Override
	public Optional<PayloadValidationException> canHandle(GenericData payload) {
        if (payload.getPayloadsAsPojo(LicenseAgreement.class) != null) {
            return Optional.empty();
        }
        
		return Optional.of(new PayloadValidationException("GenericDataToNotificationConverter only handles expiry check payload of type LicenseAgreement"));
	}
	
	@Override
	public List<IsNotification> convert(GenericData payload) {
		TemplateWithModelEmailContent<List<LicenseAgreement>> content = new TemplateWithModelEmailContent<>();
		content.setSubject("QC | These Agreements will expire soon");
		content.setTemplateFileName(properties.getEmailTemplatePath());
		
		List<LicenseAgreement> agreementsPojo = payload.getPayloadsAsPojo(LicenseAgreement.class);
		content.setModel(agreementsPojo);
		
		EmailMessageTemplated<List<LicenseAgreement>> message = new EmailMessageTemplated<>(content);
		
		message.addReceivingEndpoints(
				EmailEndpoint
						.builder()
						.email(properties.getAdminEmail())
						.build()
		);
        
		return Arrays.asList(message);
	}
	
}
