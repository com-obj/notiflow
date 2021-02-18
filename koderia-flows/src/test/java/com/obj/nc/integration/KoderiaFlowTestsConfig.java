package com.obj.nc.integration;

import com.obj.nc.config.BaseApiConfig;
import com.obj.nc.config.MailchimpApiConfig;
import com.obj.nc.mapper.RecipientMapperImpl;
import com.obj.nc.services.KoderiaServiceRestImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestGatewaySupport;

@TestConfiguration
@Import({
		MailchimpApiConfig.class,
		BaseApiConfig.class
})
@EnableConfigurationProperties(MailchimpApiConfig.class)
public class KoderiaFlowTestsConfig {

	@Autowired
	@Qualifier(MailchimpApiConfig.MAILCHIMP_REST_TEMPLATE)
	private RestTemplate mailchimpRestTemplate;

	@Bean
	public MockRestServiceServer mockMailchimpRestServer() {
		RestGatewaySupport gateway = new RestGatewaySupport();
		gateway.setRestTemplate(mailchimpRestTemplate);
		return MockRestServiceServer.createServer(gateway);
	}

}

