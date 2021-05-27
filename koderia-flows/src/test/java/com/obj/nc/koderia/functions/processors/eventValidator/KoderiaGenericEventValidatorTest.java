package com.obj.nc.koderia.functions.processors.eventValidator;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.functions.processors.eventValidator.GenericEventValidator;
import com.obj.nc.koderia.domain.event.JobPostKoderiaEventDto;
import com.obj.nc.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@JsonTest
@ContextConfiguration(classes = {
        KoderiaGenericEventValidator.class,
        KoderiaGenericEventValidatorConfigProperties.class
})
class KoderiaGenericEventValidatorTest {
    
    @Autowired private GenericEventValidator validator;
    
    @Test
    void testValidSchemaJobPostEvent() {
        // given
        String jobPostEvent = JsonUtils.readJsonStringFromClassPathResource("koderia/create_request/job_body.json");
        // when
        JsonNode validated = validator.apply(jobPostEvent);
        // then
        assertThat(validated).isNotNull();
    }
    
    @Test
    void testJobPostWithoutDescription() {
        // given
        String jobPostEventWithoutDescription = JsonUtils.readJsonStringFromClassPathResource("koderia/create_request/job_body_no_text.json");
        // when - then
        assertThatThrownBy(() -> {
            JsonNode validated = validator.apply(jobPostEventWithoutDescription);
        }).hasMessageContaining("Koderia event json does not match any known json schema");
    }
    
}