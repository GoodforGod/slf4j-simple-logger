package io.goodforgod.slf4j.simplelogger;

import java.nio.charset.Charset;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 03.04.2022
 */
final class EventEncoders {

    private EventEncoders() {}

    static final class SimpleEventEncoder implements EventEncoder {

        @Override
        public byte[] encode(SimpleLoggingEvent event) {
            final String eventAsString = event.toString();
            return eventAsString.getBytes();
        }
    }

    static final class CharsetEventEncoder implements EventEncoder {

        private final Charset charset;

        CharsetEventEncoder(Charset charset) {
            this.charset = charset;
        }

        @Override
        public byte[] encode(SimpleLoggingEvent event) {
            final String eventAsString = event.toString();
            return eventAsString.getBytes(charset);
        }
    }
}
