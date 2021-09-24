package com.obj.nc.flows.dataSources;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Validated
@Configuration
@ConfigurationProperties("nc.data-sources")
public class DataSourcesConfigurationProperties {
    
    @UniqueElements
    private List<JdbcDataSource> jdbc = new ArrayList<>();
    
    @PostConstruct
    private void setJobFullNames() {
        jdbc.forEach(jdbcDataSource -> jdbcDataSource
                .getJobs()
                .forEach(job -> job.setFullName(jdbcDataSource
                        .getName()
                        .concat(".")
                        .concat(job.getName()))));
    }
    
    @Data
    @EqualsAndHashCode(of = "name")
    @ToString(of = "name")
    static class JdbcDataSource {
        @NotEmpty
        private String name;
        @NotEmpty
        private String url;
        @NotEmpty
        private String username;
        @NotEmpty
        private String password;
        @UniqueElements
        private List<Job> jobs = new ArrayList<>();
    }
    
    @Data
    @EqualsAndHashCode(of = "name")
    @ToString(of = "name")
    static class Job {
        @NotEmpty
        private String name;
        private String fullName;
        @NotEmpty
        private String tableName;
        @NotEmpty
        private String cron;
        private String description;
        private String templatePath;
        private ExpiryCheck expiryCheck;
    }
    
    @Data
    static class ExpiryCheck {
        @NotEmpty
        private String columnName;
        @NotNull
        private long daysUntilExpiry;
    }
    
}
