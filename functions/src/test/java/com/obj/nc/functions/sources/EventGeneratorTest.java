package com.obj.nc.functions.sources;

import com.obj.nc.domain.event.Event;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sources.eventGenerator.EventGeneratorConfig;
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


@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = EventGeneratorConfig.class)
@ContextConfiguration(classes = EventGeneratorTestConfig.class)
class EventGeneratorTest {

    private static final String EVENTS_DIR = "src/test/resources/events/";
    private static final String EVENT_QUEUE_DIR = "src/test/resources/eventsQueue/";
    private static final String EVENT_FILE_NAME = "ba_job_post.json";
    private static final String FIRST_QUEUED_EVENT_FILE_NAME = "0_ba_job_post.json";

    @Autowired
    private EventGeneratorSourceSupplier generateEvent;

    @Autowired
    private EventGeneratorConfig config;

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
        Event eventFromFile = generateEvent.get();

        // then
        Assertions.assertThat(eventFromFile).isNotNull();
        Assertions.assertThat(eventFromFile.getBody().getMessage().getContent().getText()).isEqualTo("New position on koderia.sk");
        Assertions.assertThat(eventFromFile.getBody().getMessage().getContent().getSubject()).isEqualTo("New position");
        Assertions.assertThat(((Map<?, ?>) eventFromFile.getBody().getAttributes().get("originalEvent"))).hasSize(2);
        Assertions.assertThat(eventFromFile.getBody().getRecievingEndpoints()).isEmpty();
    }

    @Test
    void readEventFromInvalidFileFails() {
        // given
        config.setSourceDir(EVENT_QUEUE_DIR);
        config.setFileName(EVENT_FILE_NAME + ".JOF*&#H827hfobn");

        // when - then
        Assertions.assertThatThrownBy(generateEvent::get)
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Input Event must not be null");
    }

    @Test
    void readEventOnlyDirSpecified() {
        // given
        config.setSourceDir(EVENT_QUEUE_DIR);
        config.setFileName(null);

        // when
        Event eventFromFile = generateEvent.get();

        // then
        Assertions.assertThat(eventFromFile).isNotNull();
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
                .hasMessageContaining("Input Event must not be null");
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
        Event eventFromFile = generateEvent.get();

        // then
        Assertions.assertThat(eventFromFile.getBody().getMessage().getContent().getSubject()).isEqualTo("First event in queue");
    }

    @Test
    void readEventRemovesFileAfterRead() {
        // given
        config.setSourceDir(EVENT_QUEUE_DIR);
        config.setFileName(EVENT_FILE_NAME);

        // when - then
        Assertions.assertThat(Files.exists(Paths.get(EVENT_QUEUE_DIR + EVENT_FILE_NAME))).isTrue();

        Event eventFromFile = generateEvent.get();

        Assertions.assertThat(Files.exists(Paths.get(EVENT_QUEUE_DIR + EVENT_FILE_NAME))).isFalse();
    }

}