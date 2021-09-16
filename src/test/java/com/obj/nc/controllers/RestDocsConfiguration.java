/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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