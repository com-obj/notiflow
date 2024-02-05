package com.obj.nc.config;

import com.obj.nc.security.config.NcJwtConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static java.util.Arrays.asList;

@Configuration
@EnableSwagger2
@RequiredArgsConstructor
public class SwaggerConfiguration {
    private final NcJwtConfigProperties jwtConfigProperties;

    @Bean
    public Docket api() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.obj.nc.controllers"))
                .build();

        if (jwtConfigProperties.isEnabled()) {
            docket = docket
                    .securitySchemes(asList(new ApiKey("Bearer Token", "Authorization", "header")))
                    .securityContexts(asList(SecurityContext.builder()
                            .securityReferences(asList(
                                    new SecurityReference("Bearer Token", new AuthorizationScope[] {
                                            new AuthorizationScope("global", "accessEverything")
                                    }
                                    )))
                            .build()
                    ));
        }

        return docket;
    }
}
