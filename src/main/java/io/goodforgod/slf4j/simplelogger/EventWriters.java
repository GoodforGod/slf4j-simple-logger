package io.goodforgod.slf4j.simplelogger;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple logger writers implementations
 *
 * @author Anton Kurako (GoodforGod)
 * @since 03.04.2022
 */
final class EventWriters {

    private EventWriters() {}

    static final class SimpleEventWriter implements EventWriter {

        private final EventEncoder eventEncoder;
        private final OutputChoice outputChoice;

        SimpleEventWriter(SimpleLoggerConfiguration configuration, OutputChoice outputChoice) {
            this.outputChoice = outputChoice;
            this.eventEncoder = configuration.getEventEncoder();
        }

        @Override
        public void write(SimpleLoggingEvent event) {
            final byte[] bytes = eventEncoder.encode(event);
            try {
                outputChoice.getStream().write(bytes);
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    static final class LockAndFlushEventWriter implements EventWriter {

        private static final Lock LOCK = new ReentrantLock();

        private final EventEncoder eventEncoder;
        private final OutputChoice outputChoice;

        LockAndFlushEventWriter(SimpleLoggerConfiguration configuration, OutputChoice outputChoice) {
            this.outputChoice = outputChoice;
            this.eventEncoder = configuration.getEventEncoder();
        }

        @Override
        public void write(SimpleLoggingEvent event) {
            final byte[] bytes = eventEncoder.encode(event);
            LOCK.lock();
            try {
                outputChoice.getStream().write(bytes);
                outputChoice.getStream().flush();
            } catch (IOException e) {
                // do nothing
            } finally {
                LOCK.unlock();
            }
        }
    }
}
