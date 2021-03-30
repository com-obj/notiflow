package com.obj.nc.functions.sources;

import com.obj.nc.SystemPropertyActiveProfileResolver;
import com.obj.nc.domain.content.EmailContent;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sources.eventGenerator.EventGeneratorConfigProperties;
import com.obj.nc.functions.sources.eventGenerator.EventGeneratorSourceSupplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;


@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = EventGeneratorConfigProperties.class)
@ContextConfiguration(classes = EventGeneratorTestConfig.class)
class EventGeneratorTest {

    private static final String EVENTS_DIR = "src/test/resources/events/";
    private static final String EVENT_QUEUE_DIR = "src/test/resources/eventsQueue/";
    private static final String EVENT_FILE_NAME = "ba_job_post.json";
    private static final String FIRST_QUEUED_EVENT_FILE_NAME = "0_ba_job_post.json";

    @Autowired
    private EventGeneratorSourceSupplier generateEvent;

    @Autowired
    private EventGeneratorConfigProperties config;

    @BeforeEach
    void createAndPopulateEventsQueueDir(TestInfo testInfo) throws IOException {
        Files.createDirectory(Paths.get(EVENT_QUEUE_DIR));

        if(testInfo.getTags().contains("EmptyQueue")) {
            return;
        }

        // copy event file to queue dir
        Files.copy(Paths.get(EVENTS_DIR + EVENT_FILE_NAME),
                Paths.get(EVENT_QUEUE_DIR + EVENT_FILE_NAME),
                StandardCopyOption.REPLACE_EXISTING);
    }

    @AfterEach
    void deleteEventsQueueDir() throws IOException {
        FileSystemUtils.deleteRecursively(Paths.get(EVENT_QUEUE_DIR));
    }

    @Test
    void readEventFromValidFile() {
        // given
        config.setSourceDir(EVENT_QUEUE_DIR);
        config.setFileName(EVENT_FILE_NAME);

        // when
        NotificationIntent notificationIntentFromFile = generateEvent.get();

        // then
        Assertions.assertThat(notificationIntentFromFile).isNotNull();
        
        EmailContent emailContent = notificationIntentFromFile.getContentTyped();
        Assertions.assertThat(emailContent.getText()).isEqualTo("We are looking for a Business Intelligence (BI) Developer to create...");
        Assertions.assertThat(emailContent.getSubject()).isEqualTo("Business Intelligence (BI) Developer");
        Assertions.assertThat(((Map<?, ?>) notificationIntentFromFile.getBody().getMessage().getAttributes().get("originalEvent"))).hasSize(2);
        Assertions.assertThat(notificationIntentFromFile.getBody().getRecievingEndpoints()).isEmpty();
    }

    @Test
    void readEventFromInvalidFileFails() {
        // given
        config.setSourceDir(EVENT_QUEUE_DIR);
        config.setFileName(EVENT_FILE_NAME + ".JOFH827hfobn");

        // when - then
        Assertions.assertThatThrownBy(generateEvent::get)
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Input NotificationIntent must not be null");
    }

    @Test
    void readEventOnlyDirSpecified() {
        // given
        config.setSourceDir(EVENT_QUEUE_DIR);
        config.setFileName(null);

        // when
        NotificationIntent notificationIntentFromFile = generateEvent.get();

        // then
        Assertions.assertThat(notificationIntentFromFile).isNotNull();
    }

    @Test
    @Tag("EmptyQueue")
    void readEventOnlyDirSpecifiedEmptyQueueFails() {
        // given
        config.setSourceDir(EVENT_QUEUE_DIR);
        config.setFileName(null);

        // when - then
        Assertions.assertThatThrownBy(generateEvent::get)
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Input NotificationIntent must not be null");
    }

    @Test
    void readEventOnlyDirSpecifiedFirstEventAlphabetically() throws IOException {
        // copy first event file (alphabetically) to queue dir
        Files.copy(Paths.get(EVENTS_DIR + FIRST_QUEUED_EVENT_FILE_NAME),
                Paths.get(EVENT_QUEUE_DIR + FIRST_QUEUED_EVENT_FILE_NAME),
                StandardCopyOption.REPLACE_EXISTING);

        // given
        config.setSourceDir(EVENT_QUEUE_DIR);
        config.setFileName(null);

        // when
        NotificationIntent notificationIntentFromFile = generateEvent.get();

        // then
        EmailContent emailContent = notificationIntentFromFile.getContentTyped();
        Assertions.assertThat(emailContent.getSubject()).isEqualTo("First event in queue");
    }

    @Test
    void readEventRemovesFileAfterRead() {
        // given
        config.setSourceDir(EVENT_QUEUE_DIR);
        config.setFileName(EVENT_FILE_NAME);

        // when - then
        Assertions.assertThat(Files.exists(Paths.get(EVENT_QUEUE_DIR + EVENT_FILE_NAME))).isTrue();

        NotificationIntent notificationIntentFromFile = generateEvent.get();

        Assertions.assertThat(Files.exists(Paths.get(EVENT_QUEUE_DIR + EVENT_FILE_NAME))).isFalse();
    }

}