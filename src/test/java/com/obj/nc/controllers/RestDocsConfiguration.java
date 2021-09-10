package com.obj.nc.controllers;

import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.restdocs.cli.CliDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcOperationPreprocessorsConfigurer;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentationConfigurer;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.templates.TemplateFormats;

@TestConfiguration
public class RestDocsConfiguration {
    @Bean
    public RestDocsMockMvcConfigurationCustomizer restDocsMockMvcConfigurationCustomizer() {
        return customizer -> configureRestDocs(customizer);
    }
    
public static MockMvcOperationPreprocessorsConfigurer configureRestDocs(MockMvcRestDocumentationConfigurer customizer) {
	return customizer
				.snippets()
				.withDefaults(
						CliDocumentation.curlRequest(),
						CliDocumentation.httpieRequest(),
						PayloadDocumentation.requestBody(), 
						PayloadDocumentation.responseBody())
				.withTemplateFormat(TemplateFormats.markdown())
				.and()
				.operationPreprocessors()
				.withResponseDefaults(Preprocessors.prettyPrint())
				.withRequestDefaults(Preprocessors.prettyPrint());
}
}