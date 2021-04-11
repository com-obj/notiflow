package com.obj.nc.config;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.converters.JsonNodeToPgObjectConverter;
import com.obj.nc.repositories.converters.PgObjectToJsonNodeConverter;

@Configuration
@EnableJdbcRepositories(basePackageClasses = GenericEventRepository.class)
@EnableJdbcAuditing
public class JdbcConfiguration extends AbstractJdbcConfiguration {

    @Bean
    public NamedParameterJdbcOperations namedParameterJdbcOperations(DataSource dataSource) { 
        return new NamedParameterJdbcTemplate(dataSource);
    }
    
    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
    	List<Converter<?, ?>> converters = new ArrayList<>();
    	converters.add(new JsonNodeToPgObjectConverter());
    	converters.add(new PgObjectToJsonNodeConverter());
    
    	return new JdbcCustomConversions(converters);
    }
    
    @Bean
    public FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
