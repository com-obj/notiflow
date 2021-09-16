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

package com.obj.nc.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.repositories.converters.ContentToPgObjectConverter;
import com.obj.nc.repositories.converters.IntentContentToPgObjectConverter;
import com.obj.nc.repositories.converters.JsonNodeToPgObjectConverter;
import com.obj.nc.repositories.converters.PgObjectToContentConverter;
import com.obj.nc.repositories.converters.PgObjectToIntentContentConverter;
import com.obj.nc.repositories.converters.PgObjectToJsonNodeConverter;
import com.obj.nc.repositories.converters.PgObjectToUUIDArrayConverter;
import com.obj.nc.repositories.converters.UUIDArrayToPgObjectConverter;

@Configuration
@EnableJdbcRepositories(basePackageClasses = GenericEventRepository.class)
@EnableJdbcAuditing
public class JdbcConfiguration extends AbstractJdbcConfiguration {

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) { 
        return new NamedParameterJdbcTemplate(dataSource);
    }
    
    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
    	List<Converter<?, ?>> converters = new ArrayList<>();
    	converters.add(new JsonNodeToPgObjectConverter());
    	converters.add(new PgObjectToJsonNodeConverter());
    	
    	converters.add(new UUIDArrayToPgObjectConverter());
    	converters.add(new PgObjectToUUIDArrayConverter());
    	
    	converters.add(new ContentToPgObjectConverter());
    	converters.add(new PgObjectToContentConverter());

    	converters.add(new IntentContentToPgObjectConverter());
    	converters.add(new PgObjectToIntentContentConverter());

    	return new JdbcCustomConversions(converters);
    }
    
    @Bean
    public NullAuditorBean jdbcAuditor() {
    	return new NullAuditorBean();
    }
    
    @Bean
    public FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
    
    public class NullAuditorBean implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            return Optional.empty();
        }
    }
}
