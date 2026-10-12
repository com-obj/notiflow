package com.obj.nc.config;

import com.obj.nc.security.config.NcJwtConfigProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI notificationCenterApi(NcJwtConfigProperties jwtProperties) {
        var api = new OpenAPI().info(new Info().title("Notification Center").version("1"));
        if (jwtProperties.isEnabled()) {
            api.components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                    .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
        }
        return api;
    }
}
