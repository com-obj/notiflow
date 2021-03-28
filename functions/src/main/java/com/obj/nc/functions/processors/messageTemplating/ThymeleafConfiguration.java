package com.obj.nc.functions.processors.messageTemplating;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

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

import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Configuration
@Data
@Log4j2
public class ThymeleafConfiguration {

	private static final String EMAIL_TEMPLATE_ENCODING = "UTF-8";
	
	@Autowired private ThymeleafConfigProperties config;
    @Autowired private SpringTemplateEngine templateEngine;

	@Bean(name = "nc.emailTemplateFormatter.messageSource")
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
