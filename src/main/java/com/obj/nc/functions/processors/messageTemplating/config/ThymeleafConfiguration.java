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

package com.obj.nc.functions.processors.messageTemplating.config;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import javax.annotation.PostConstruct;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Configuration
@Data
@Log4j2
public class ThymeleafConfiguration {

	private static final String EMAIL_TEMPLATE_ENCODING = "UTF-8";
	
	public static final String MESSAGE_SOURCE_FOR_TEMPLATES_BEAN_NAME = "nc.emailTemplateFormatter.messageSource";
	
	@Autowired private ThymeleafConfigProperties config;
    @Autowired private SpringTemplateEngine templateEngine;

	@Bean(name = MESSAGE_SOURCE_FOR_TEMPLATES_BEAN_NAME)
	public ReloadableResourceBundleMessageSource emailMessageSource() {
		log.info("Configuring i18n message source to be have basename (path end file name prefix) " + config.getMessagesDirAndBaseName());
		
		final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.addBasenames("classpath:nc-internal-resources/messages");
		
		if (config.getMessagesDirAndBaseName()!=null) {
			messageSource.addBasenames(config.getMessagesDirAndBaseName());
		}

		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}
	
	
	@PostConstruct
    public TemplateEngine configure() {
        // Resolver for TEXT emails
		int resloverIndex = 1;
		textTemplateResolver(resloverIndex).forEach(res -> templateEngine.addTemplateResolver( res ));
        // Resolver for HTML emails
		htmlTemplateResolver(resloverIndex).forEach(res -> templateEngine.addTemplateResolver( res ));
		// Resolver for internal HTML emails
		templateEngine.addTemplateResolver(internalhtmlTemplateResolver());
        // Message source, internationalization specific to emails
        templateEngine.setTemplateEngineMessageSource(emailMessageSource());
        return templateEngine;
    }
    
    private List<ITemplateResolver> textTemplateResolver(int startIndex) {
    	List<ITemplateResolver> resolvers = new ArrayList<>();
    	
    	if (config.getTemplatesRootDir()== null) {
    		return resolvers;
    	}

    	for (String templateDir: config.getTemplatesRootDir()) {
			log.info("Configuring Thymeleaf template resolver root path to be " + templateDir  + File.separator);
			
	        final FileTemplateResolver templateResolver = new FileTemplateResolver();
	        templateResolver.setOrder(startIndex++);
	        templateResolver.setPrefix(templateDir + File.separator);
	        templateResolver.setSuffix(".txt");
	        templateResolver.setTemplateMode(TemplateMode.TEXT);
	        templateResolver.setCharacterEncoding(EMAIL_TEMPLATE_ENCODING);
	        templateResolver.setCacheable(false);
	        templateResolver.setCheckExistence(true);
	        
	        resolvers.add(templateResolver) ;
    	}
    	return resolvers;
    }

    private List<ITemplateResolver> htmlTemplateResolver(int startIndex) {
    	List<ITemplateResolver> resolvers = new ArrayList<>();
    	
    	if (config.getTemplatesRootDir()== null) {
    		return resolvers;
    	}
    	
    	for (String templateDir: config.getTemplatesRootDir()) {
			log.info("Configuring Thymeleaf template resolver root path to be " + templateDir  + File.separator);
			
	        final FileTemplateResolver templateResolver = new FileTemplateResolver();
	        templateResolver.setOrder(startIndex);
	        templateResolver.setPrefix(templateDir  + File.separator);
	        templateResolver.setSuffix(".html");
	        templateResolver.setTemplateMode(TemplateMode.HTML);
	        templateResolver.setCharacterEncoding(EMAIL_TEMPLATE_ENCODING);
	        templateResolver.setCacheable(false);
	        templateResolver.setCheckExistence(true);
	        
	        resolvers.add(templateResolver) ;
    	}
    	return resolvers;
    }
    
    private ITemplateResolver internalhtmlTemplateResolver() {
			log.info("Configuring Thymeleaf template resolver root path to be classpath:nc-internal-resources/message-templates/ serving internal templates");
			
			ClassLoaderTemplateResolver internalTemplateResolver = new ClassLoaderTemplateResolver();
			internalTemplateResolver.setOrder(Integer.valueOf(999));
			internalTemplateResolver.setPrefix("nc-internal-resources/message-templates/");
			internalTemplateResolver.setSuffix(".html");
			internalTemplateResolver.setTemplateMode(TemplateMode.HTML);
			internalTemplateResolver.setCharacterEncoding(EMAIL_TEMPLATE_ENCODING);
			internalTemplateResolver.setCacheable(true);
			internalTemplateResolver.setCheckExistence(true);
	        
	        return internalTemplateResolver;
    }
    
    public List<Locale> getDefaultLocales() {
    	if (CollectionUtils.isEmpty(config.getDefaultLocaleCodes())) {
    		return Arrays.asList(Locale.getDefault());
    	}
    	
    	return config.getDefaultLocaleCodes().stream().map(locStr -> new Locale(locStr)).collect(Collectors.toList());
    }


}
