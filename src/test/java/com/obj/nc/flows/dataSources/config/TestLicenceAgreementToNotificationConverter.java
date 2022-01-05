package com.obj.nc.flows.dataSources.config;

import com.obj.nc.domain.IsNotification;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.message.EmailMessageTemplated;
import com.obj.nc.domain.pullNotifData.PullNotifData;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.extensions.converters.pullNotifData.PullNotifData2NotificationConverterExtension;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class TestLicenceAgreementToNotificationConverter implements PullNotifData2NotificationConverterExtension<TestLicenseAgreement> {
	
	private final TestLicenseAgreementProperties properties;
	
	@Override
	public Optional<PayloadValidationException> canHandle(PullNotifData<TestLicenseAgreement> data) {
        if (data.getPayloads() != null) {
            return Optional.empty();
        }
        
		return Optional.of(new PayloadValidationException("PullNotifDataToNotificationConverter only handles expiry check payload of type LicenseAgreement"));
	}
	
	@Override
	public List<IsNotification> convert(PullNotifData<TestLicenseAgreement> data) {
		TemplateWithModelEmailContent<List<TestLicenseAgreement>> content = new TemplateWithModelEmailContent<>();
		content.setSubject("QC | These Agreements will expire soon");
		content.setTemplateFileName(properties.getEmailTemplatePath());
		
		List<TestLicenseAgreement> agreementsPojo = data.getPayloads();
		content.setModel(agreementsPojo);
		
		EmailMessageTemplated<List<TestLicenseAgreement>> message = new EmailMessageTemplated<>(content);
		
		message.addReceivingEndpoints(
				EmailEndpoint
						.builder()
						.email(properties.getAdminEmail())
						.build()
		);
        
		return Arrays.asList(message);
	}

	@Override
	public Class<TestLicenseAgreement> getPayloadType() {
		return TestLicenseAgreement.class;
	}
	
}
