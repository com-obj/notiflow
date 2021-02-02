package com.obj.nc.functions.sources;

import com.obj.nc.domain.event.Event;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.sources.eventGenerator.EventGeneratorConfig;
import com.obj.nc.functions.sources.eventGenerator.EventGeneratorExecution;
import com.obj.nc.functions.sources.eventGenerator.EventGeneratorPreCondition;
import com.obj.nc.functions.sources.eventGenerator.EventGeneratorSourceSupplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


class EventGeneratorTest {

    private static final String EVENTS_DIR = "src/test/resources/events/";
    private static final String EVENT_QUEUE_DIR = "src/test/resources/eventsQueue/";
    private static final String EVENT_FILE_NAME = "ba_job_post.json";
    private static final String FIRST_QUEUED_EVENT_FILE_NAME = "0_ba_job_post.json";

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
        EventGeneratorConfig eventSourceConfig = new EventGeneratorConfig();
        eventSourceConfig.setSourceDir(EVENT_QUEUE_DIR);
        eventSourceConfig.setFileName(EVENT_FILE_NAME);

        EventGeneratorSourceSupplier eventGenerator = new EventGeneratorSourceSupplier(
                new EventGeneratorExecution(),
                new EventGeneratorPreCondition());

        eventGenerator.setEventSourceConfig(eventSourceConfig);

        // when
        Event eventFromFile = eventGenerator.get();

        // then
        Assertions.assertThat(eventFromFile).isNotNull();
        Assertions.assertThat(eventFromFile.getBody().getMessage().getText()).isEqualTo("New position on koderia.sk");
        Assertions.assertThat(eventFromFile.getBody().getMessage().getSubject()).isEqualTo("New position");
        Assertions.assertThat(eventFromFile.getBody().getAttributes()).hasSize(2);
        Assertions.assertThat(eventFromFile.getBody().getRecievingEndpoints()).isEmpty();
    }

    @Test
    void readEventFromInvalidFileFails() {
        // given
        EventGeneratorConfig eventSourceConfig = new EventGeneratorConfig();
        eventSourceConfig.setSourceDir(EVENT_QUEUE_DIR);
        eventSourceConfig.setFileName(EVENT_FILE_NAME + ".JOF*&#H827hfobn");

        EventGeneratorSourceSupplier eventGenerator = new EventGeneratorSourceSupplier(
                new EventGeneratorExecution(),
                new EventGeneratorPreCondition());

        eventGenerator.setEventSourceConfig(eventSourceConfig);

        // when - then
        Assertions.assertThatThrownBy(eventGenerator::get)
                .isInstanceOf(PayloadValidationException.class)
                .hasMessageContaining("Input Event must not be null");
    }

    @Test
    void readEventOnlyDirSpecified() {
        // given
        EventGeneratorConfig eventSourceConfig = new EventGeneratorConfig();
        eventSourceConfig.setSourceDir(EVENT_QUEUE_DIR);
        eventSourceConfig.setFileName(null);

        EventGeneratorSourceSupplier eventGenerator = new EventGeneratorSourceSupplier(
                new EventGeneratorExecution(),
                new EventGeneratorPreCondition());

        eventGenerator.setEventSourceConfig(eventSourceConfig);

        // when
        Event eventFromFile = eventGenerator.get();

        // then
        Assertions.assertThat(eventFromFile).isNotNull();
    }

    @Test
    @Tag("EmptyQueue")
    void readEventOnlyDirSpecifiedEmptyQueueFails() {
        // given
        EventGeneratorConfig eventSourceConfig = new EventGeneratorConfig();
        eventSourceConfig.setSourceDir(EVENT_QUEUE_DIR);
        eventSourceConfig.setFileName(null);

        EventGeneratorSourceSupplier eventGenerator = new EventGeneratorSourceSupplier(
                new EventGeneratorExecution(),
                new EventGeneratorPreCondition());

        eventGenerator.setEventSourceConfig(eventSourceConfig);

        // when - then
        Assertions.assertThatThrownBy(eventGenerator::get)
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
        EventGeneratorConfig eventSourceConfig = new EventGeneratorConfig();
        eventSourceConfig.setSourceDir(EVENT_QUEUE_DIR);
        eventSourceConfig.setFileName(null);

        EventGeneratorSourceSupplier eventGenerator = new EventGeneratorSourceSupplier(
                new EventGeneratorExecution(),
                new EventGeneratorPreCondition());

        eventGenerator.setEventSourceConfig(eventSourceConfig);

        // when
        Event eventFromFile = eventGenerator.get();

        // then
        Assertions.assertThat(eventFromFile.getBody().getMessage().getSubject()).isEqualTo("First event in queue");
    }

    @Test
    void readEventRemovesFileAfterRead() {
        // given
        EventGeneratorConfig eventSourceConfig = new EventGeneratorConfig();
        eventSourceConfig.setSourceDir(EVENT_QUEUE_DIR);
        eventSourceConfig.setFileName(EVENT_FILE_NAME);

        EventGeneratorSourceSupplier eventGenerator = new EventGeneratorSourceSupplier(
                new EventGeneratorExecution(),
                new EventGeneratorPreCondition());

        eventGenerator.setEventSourceConfig(eventSourceConfig);

        // when - then
        Assertions.assertThat(Files.exists(Paths.get(EVENT_QUEUE_DIR + EVENT_FILE_NAME))).isTrue();

        Event eventFromFile = eventGenerator.get();

        Assertions.assertThat(Files.exists(Paths.get(EVENT_QUEUE_DIR + EVENT_FILE_NAME))).isFalse();
    }

}