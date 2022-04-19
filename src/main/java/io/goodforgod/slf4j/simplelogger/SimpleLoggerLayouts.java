package io.goodforgod.slf4j.simplelogger;

import java.time.*;
import org.slf4j.event.Level;

/**
 * Default SimpleLogger layout implementations
 *
 * @author Anton Kurako (GoodforGod)
 * @since 14.03.2022
 */
final class SimpleLoggerLayouts {

    private SimpleLoggerLayouts() {}

    /**
     * Uses {@link LayoutOrder#ordinal()} for ordering layouts between each other
     */
    enum LayoutOrder {
        DATE_TIME,
        IMPLEMENTATION,
        LEVEL,
        ENVIRONMENT,
        THREAD,
        LOGGER_NAME,
        MESSAGE,
        EVENT_SEPARATOR,
        THROWABLE
    }

    private static final class DateTimeCache {

        private final String formatted;
        private final long epochMillis;

        private DateTimeCache(String formatted, long epochMillis) {
            this.formatted = formatted;
            this.epochMillis = epochMillis;
        }
    }

    private abstract static class AbstractTimeLayout implements Layout {

        private volatile DateTimeCache cache = new DateTimeCache(null, -1);

        abstract String format(long eventCreatMillis);

        String getEventTime(SimpleLoggingEvent event) {
            final DateTimeCache cacheLocal = this.cache;
            if (cacheLocal.epochMillis == event.created()) {
                return cacheLocal.formatted;
            } else {
                final String formatted = format(event.created());
                this.cache = new DateTimeCache(formatted, event.created());
                return formatted;
            }
        }
    }

    static class DateTimeLayout extends AbstractTimeLayout {

        private final SimpleLoggerConfiguration configuration;

        protected DateTimeLayout(SimpleLoggerConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append(getEventTime(event));
            event.append(' ');
        }

        /**
         * @param eventCreatMillis from epoch
         * @see LocalTime#ofNanoOfDay(long)
         * @return formatter date time
         */
        @Override
        String format(long eventCreatMillis) {
            final Instant now = Instant.ofEpochMilli(eventCreatMillis);
            final ZoneId zoneId = configuration.getZoneId();
            final LocalDateTime dateTime = LocalDateTime.ofInstant(now, zoneId);
            return configuration.getDateTimeFormatter().format(dateTime);
        }

        @Override
        public int order() {
            return LayoutOrder.DATE_TIME.ordinal();
        }
    }

    static class TimeLayout extends AbstractTimeLayout {

        private final SimpleLoggerConfiguration configuration;

        protected TimeLayout(SimpleLoggerConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append(getEventTime(event));
            event.append(' ');
        }

        /**
         * @param eventCreatMillis from epoch
         * @see LocalTime#ofInstant(Instant, ZoneId)
         * @return formatter date time
         */
        @Override
        String format(long eventCreatMillis) {
            final Instant now = Instant.ofEpochMilli(eventCreatMillis);
            final ZoneId zoneId = configuration.getZoneId();
            final LocalTime localTime = LocalTime.ofInstant(now, zoneId);
            return configuration.getDateTimeFormatter().format(localTime);
        }

        @Override
        public int order() {
            return LayoutOrder.DATE_TIME.ordinal();
        }
    }

    static final class UnixTimeLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append(System.currentTimeMillis());
            event.append(' ');
        }

        @Override
        public int order() {
            return LayoutOrder.DATE_TIME.ordinal();
        }
    }

    static final class MillisFromStartLayout implements Layout {

        private final SimpleLoggerConfiguration configuration;

        MillisFromStartLayout(SimpleLoggerConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append(System.currentTimeMillis() - configuration.getInitializeTime());
            event.append(' ');
        }

        @Override
        public int order() {
            return LayoutOrder.DATE_TIME.ordinal();
        }
    }

    static final class ImplementationLayout implements Layout {

        private final SimpleLoggerConfiguration configuration;

        ImplementationLayout(SimpleLoggerConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append("[");
            event.append(configuration.getImplementationVersion());
            event.append("] ");
        }

        @Override
        public int order() {
            return LayoutOrder.IMPLEMENTATION.ordinal();
        }
    }

    static class LevelLayout implements Layout {

        private final String trace;
        private final String debug;
        private final String info;
        private final String warn;
        private final String error;

        LevelLayout(String trace, String debug, String info, String warn, String error) {
            this.trace = trace;
            this.debug = debug;
            this.info = info;
            this.warn = warn;
            this.error = error;
        }

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append(renderLevel(event.level()));
        }

        protected String renderLevel(Level level) {
            switch (level) {
                case INFO:
                    return info;
                case WARN:
                    return warn;
                case ERROR:
                    return error;
                case DEBUG:
                    return debug;
                case TRACE:
                    return trace;
                default:
                    throw new IllegalStateException("Unrecognized level [" + level + "]");
            }
        }

        @Override
        public int order() {
            return LayoutOrder.LEVEL.ordinal();
        }
    }

    static final class EnvironmentOnStartLayout implements Layout {

        private final SimpleLoggerConfiguration configuration;

        EnvironmentOnStartLayout(SimpleLoggerConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append(configuration.getEnvironmentsOnStartText());
        }

        @Override
        public int order() {
            return LayoutOrder.ENVIRONMENT.ordinal();
        }
    }

    static final class EnvironmentLayout implements Layout {

        private final SimpleLoggerConfiguration configuration;

        EnvironmentLayout(SimpleLoggerConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public void print(SimpleLoggingEvent event) {
            boolean bracketUsed = false;
            for (String envName : configuration.getEnvironments()) {
                final String envValue = System.getenv(envName);
                if (envValue == null && !configuration.isEnvironmentShowNullable()) {
                    continue;
                }

                if (!bracketUsed) {
                    event.append('[');
                    bracketUsed = true;
                } else {
                    event.append(", ");
                }

                if (configuration.isEnvironmentShowName()) {
                    event.append(envName);
                    event.append('=');
                }

                event.append(envValue);
            }

            if (bracketUsed) {
                event.append("] ");
            }
        }

        @Override
        public int order() {
            return LayoutOrder.ENVIRONMENT.ordinal();
        }
    }

    static final class ThreadLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append('[');
            event.append(Thread.currentThread().getName());
            event.append("] ");
        }

        @Override
        public int order() {
            return LayoutOrder.THREAD.ordinal();
        }
    }

    static final class LoggerNameLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append(event.logger());
            event.append(" - ");
        }

        @Override
        public int order() {
            return LayoutOrder.LOGGER_NAME.ordinal();
        }
    }

    static final class MessageLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append(event.message());
        }

        @Override
        public int order() {
            return LayoutOrder.MESSAGE.ordinal();
        }
    }

    static final class SeparatorLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append(System.lineSeparator());
        }

        @Override
        public int order() {
            return LayoutOrder.EVENT_SEPARATOR.ordinal();
        }
    }

    static final class ThrowableLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            final Throwable throwable = event.throwable();
            if (throwable != null) {
                event.append(throwable);
            }
        }

        @Override
        public int order() {
            return LayoutOrder.THROWABLE.ordinal();
        }
    }
}
