package com.obj.nc.integration;

import com.obj.nc.config.BaseApiConfig;
import com.obj.nc.config.MailchimpApiConfig;
import com.obj.nc.services.MailchimpServiceRestImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.client.MockRestServiceServer;

@TestConfiguration
@Import(BaseApiConfig.class)
@EnableConfigurationProperties(MailchimpApiConfig.class)
public class KoderiaFlowTestsConfig {

	@Autowired
	private MockServerRestTemplateCustomizer customizer;

	@Autowired
	private MailchimpServiceRestImpl mailchimpServiceRest;

	@Primary
	@Bean
	public MockRestServiceServer mockMailchimpRestServer() {
		return customizer.getServer(mailchimpServiceRest.getRestTemplate());
	}

}

