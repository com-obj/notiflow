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
public class TestCvToNotificationConverter implements PullNotifData2NotificationConverterExtension<TestCv> {

    private final TestLicenseAgreementProperties properties;

    @Override
    public Optional<PayloadValidationException> canHandle(PullNotifData<TestCv> data) {
        if (data.getPayloads() != null) {
            return Optional.empty();
        }

        return Optional.of(new PayloadValidationException("PullNotifDataToNotificationConverter only handles expiry check payload of type TestCv"));
    }

    @Override
    public List<IsNotification> convert(PullNotifData<TestCv> data) {
        TemplateWithModelEmailContent<List<TestCv>> content = new TemplateWithModelEmailContent<>();
        content.setSubject("CVs");
        content.setTemplateFileName(properties.getEmailTemplatePath());

        List<TestCv> agreementsPojo = data.getPayloads();
        content.setModel(agreementsPojo);

        EmailMessageTemplated<List<TestCv>> message = new EmailMessageTemplated<>(content);

        message.addReceivingEndpoints(
                EmailEndpoint
                        .builder()
                        .email(properties.getAdminEmail())
                        .build()
        );

        return Arrays.asList(message);
    }

    @Override
    public Class<TestCv> getPayloadType() {
        return TestCv.class;
    }

}
