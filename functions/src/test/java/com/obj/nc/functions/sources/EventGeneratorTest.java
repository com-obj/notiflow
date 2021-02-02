package com.obj.nc.functions.sources;

import com.obj.nc.domain.event.Event;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

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
    void copyEventToQueue(TestInfo testInfo) throws IOException {
        if(testInfo.getTags().contains("SkipCopyEventToQueue")) {
            return;
        }

        // copy event file to queue dir
        Files.copy(Paths.get(EVENTS_DIR + EVENT_FILE_NAME),
                Paths.get(EVENT_QUEUE_DIR + EVENT_FILE_NAME),
                StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    void readEventFromValidFile() {
        // given
        EventGenerator.EventSourceConfig eventSourceConfig = new EventGenerator.EventSourceConfig();
        eventSourceConfig.setSourceDir(EVENT_QUEUE_DIR);
        eventSourceConfig.setFileName(EVENT_FILE_NAME);

        EventGenerator eventGenerator = new EventGenerator();
        eventGenerator.setEventSourceConfig(eventSourceConfig);

        // when
        Event eventFromFile = eventGenerator.readEventsFromFile();

        // then
        Assertions.assertThat(eventFromFile).isNotNull();
        Assertions.assertThat(eventFromFile.getBody().getMessage().getText()).isEqualTo("New position on koderia.sk");
        Assertions.assertThat(eventFromFile.getBody().getMessage().getSubject()).isEqualTo("New position");
        Assertions.assertThat(eventFromFile.getBody().getAttributes()).hasSize(2);
        Assertions.assertThat(eventFromFile.getBody().getRecievingEndpoints()).isEmpty();
    }

    @Test
    void readEventFromInvalidFileIsNull() {
        // given
        EventGenerator.EventSourceConfig eventSourceConfig = new EventGenerator.EventSourceConfig();
        eventSourceConfig.setSourceDir(EVENT_QUEUE_DIR);
        eventSourceConfig.setFileName(EVENT_FILE_NAME + ".JOF*&#H827hfobn");

        EventGenerator eventGenerator = new EventGenerator();
        eventGenerator.setEventSourceConfig(eventSourceConfig);

        // when
        Event eventFromFile = eventGenerator.readEventsFromFile();

        // then
        Assertions.assertThat(eventFromFile).isNull();
    }

    @Test
    void readEventOnlyDirSpecified() {
        // given
        EventGenerator.EventSourceConfig eventSourceConfig = new EventGenerator.EventSourceConfig();
        eventSourceConfig.setSourceDir(EVENT_QUEUE_DIR);
        eventSourceConfig.setFileName(null);

        EventGenerator eventGenerator = new EventGenerator();
        eventGenerator.setEventSourceConfig(eventSourceConfig);

        // when
        Event eventFromFile = eventGenerator.readEventsFromFile();

        // then
        Assertions.assertThat(eventFromFile).isNotNull();
    }

    @Test
    @Tag("SkipCopyEventToQueue")
    void readEventOnlyDirSpecifiedEmptyQueueIsNull() {
        // given
        EventGenerator.EventSourceConfig eventSourceConfig = new EventGenerator.EventSourceConfig();
        eventSourceConfig.setSourceDir(EVENT_QUEUE_DIR);
        eventSourceConfig.setFileName(null);

        EventGenerator eventGenerator = new EventGenerator();
        eventGenerator.setEventSourceConfig(eventSourceConfig);

        // when
        Event eventFromFile = eventGenerator.readEventsFromFile();

        // then
        Assertions.assertThat(eventFromFile).isNull();
    }

    @Test
    void readEventOnlyDirSpecifiedFirstEventAlphabetically() throws IOException {
        // copy first event file (alphabetically) to queue dir
        Files.copy(Paths.get(EVENTS_DIR + FIRST_QUEUED_EVENT_FILE_NAME),
                Paths.get(EVENT_QUEUE_DIR + FIRST_QUEUED_EVENT_FILE_NAME),
                StandardCopyOption.REPLACE_EXISTING);

        // given
        EventGenerator.EventSourceConfig eventSourceConfig = new EventGenerator.EventSourceConfig();
        eventSourceConfig.setSourceDir(EVENT_QUEUE_DIR);
        eventSourceConfig.setFileName(null);

        EventGenerator eventGenerator = new EventGenerator();
        eventGenerator.setEventSourceConfig(eventSourceConfig);

        // when
        Event eventFromFile = eventGenerator.readEventsFromFile();

        // then
        Assertions.assertThat(eventFromFile.getBody().getMessage().getSubject()).isEqualTo("First event in queue");
    }

    @Test
    void readEventRemovesFileAfterRead() {
        // given
        EventGenerator.EventSourceConfig eventSourceConfig = new EventGenerator.EventSourceConfig();
        eventSourceConfig.setSourceDir(EVENT_QUEUE_DIR);
        eventSourceConfig.setFileName(EVENT_FILE_NAME);

        EventGenerator eventGenerator = new EventGenerator();
        eventGenerator.setEventSourceConfig(eventSourceConfig);

        // when - then
        Assertions.assertThat(Files.exists(Paths.get(EVENT_QUEUE_DIR + EVENT_FILE_NAME))).isTrue();

        Event eventFromFile = eventGenerator.readEventsFromFile();

        Assertions.assertThat(Files.exists(Paths.get(EVENT_QUEUE_DIR + EVENT_FILE_NAME))).isFalse();
    }

}