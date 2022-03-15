package io.goodforgod.slf4j.simplelogger;

import java.time.*;

/**
 * Default SimpleLogger layout implementations
 *
 * @author Anton Kurako (GoodforGod)
 * @since 14.03.2022
 */
final class SimpleLoggerLayouts {

    private static final long NANOS_PER_SECOND = 1000_000_000L;
    private static final int SECONDS_PER_DAY = 60 * 60 * 24;

    private SimpleLoggerLayouts() {}

    /**
     * Uses {@link LayoutOrder#ordinal()} for ordering layouts between each other
     */
    private enum LayoutOrder {
        DATE_TIME,
        IMPLEMENTATION,
        LEVEL,
        ENVIRONMENT,
        THREAD,
        LOGGER_NAME
    }

    private static final class DateTimeCache {

        private final long epochMillis;
        private final String formatted;

        private DateTimeCache(long epochMillis, String formatted) {
            this.epochMillis = epochMillis;
            this.formatted = formatted;
        }
    }

    static final class DateTimeLayout implements Layout {

        private final SimpleLoggerConfiguration configuration;
        private volatile DateTimeCache cache = new DateTimeCache(-1, null);

        DateTimeLayout(SimpleLoggerConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public void print(String loggerName, int level, StringBuilder builder) {
            builder.append(getFormattedDateTime());
            builder.append(' ');
        }

        /**
         * @see LocalDateTime#ofEpochSecond(long, int, ZoneOffset)
         * @return formatter date time
         */
        private String getFormattedDateTime() {
            final DateTimeCache localCache = this.cache;
            final long epochMilli = System.currentTimeMillis();

            if (localCache.epochMillis == epochMilli) {
                return localCache.formatted;
            } else {
                final Instant now = Instant.ofEpochMilli(epochMilli);
                final ZoneOffset offset = Clock.systemDefaultZone().getZone().getRules().getOffset(now);
                final long localSecond = now.getEpochSecond() + offset.getTotalSeconds(); // overflow caught later
                final long localEpochDay = Math.floorDiv(localSecond, SECONDS_PER_DAY);
                final int secsOfDay = Math.floorMod(localSecond, SECONDS_PER_DAY);
                final LocalDate date = LocalDate.ofEpochDay(localEpochDay);
                final LocalTime time = LocalTime.ofNanoOfDay(secsOfDay * NANOS_PER_SECOND + now.getNano());
                final LocalDateTime dateTime = LocalDateTime.of(date, time);

                final String formatted = configuration.getDateTimeFormatter().format(dateTime);
                this.cache = new DateTimeCache(epochMilli, formatted);
                return formatted;
            }
        }

        @Override
        public int order() {
            return LayoutOrder.DATE_TIME.ordinal();
        }
    }

    static final class TimeLayout implements Layout {

        private final SimpleLoggerConfiguration configuration;
        private volatile DateTimeCache cache = new DateTimeCache(-1, null);

        TimeLayout(SimpleLoggerConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public void print(String loggerName, int level, StringBuilder builder) {
            builder.append(getFormattedTime());
            builder.append(' ');
        }

        /**
         * @see LocalTime#ofInstant(Instant, ZoneId)
         * @return formatter date time
         */
        private String getFormattedTime() {
            final DateTimeCache localCache = this.cache;
            final long epochMilli = System.currentTimeMillis();

            if (localCache.epochMillis == epochMilli) {
                return localCache.formatted;
            } else {
                final Instant now = Instant.ofEpochMilli(epochMilli);
                final ZoneOffset offset = Clock.systemDefaultZone().getZone().getRules().getOffset(now);
                final long localSecond = now.getEpochSecond() + offset.getTotalSeconds();
                final int secsOfDay = Math.floorMod(localSecond, SECONDS_PER_DAY);
                final LocalTime localTime = LocalTime.ofNanoOfDay(secsOfDay * NANOS_PER_SECOND + now.getNano());

                final String formatted = configuration.getDateTimeFormatter().format(localTime);
                this.cache = new DateTimeCache(epochMilli, formatted);
                return formatted;
            }
        }

        @Override
        public int order() {
            return LayoutOrder.DATE_TIME.ordinal();
        }
    }

    static final class UnixTimeLayout implements Layout {

        @Override
        public void print(String loggerName, int level, StringBuilder builder) {
            builder.append(System.currentTimeMillis());
            builder.append(' ');
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
        public void print(String loggerName, int level, StringBuilder builder) {
            builder.append(System.currentTimeMillis() - configuration.getInitializeTime());
            builder.append(' ');
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
        public void print(String loggerName, int level, StringBuilder builder) {
            builder.append(configuration.getImplementationVersion());
        }

        @Override
        public int order() {
            return LayoutOrder.IMPLEMENTATION.ordinal();
        }
    }

    static final class LevelLayout implements Layout {

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
        public void print(String loggerName, int level, StringBuilder builder) {
            builder.append(renderLevel(level));
        }

        private String renderLevel(int level) {
            switch (level) {
                case SimpleLogger.LOG_LEVEL_INFO:
                    return info;
                case SimpleLogger.LOG_LEVEL_WARN:
                    return warn;
                case SimpleLogger.LOG_LEVEL_ERROR:
                    return error;
                case SimpleLogger.LOG_LEVEL_DEBUG:
                    return debug;
                case SimpleLogger.LOG_LEVEL_TRACE:
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
        public void print(String loggerName, int level, StringBuilder builder) {
            builder.append(configuration.getEnvironmentsOnStart());
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
        public void print(String loggerName, int level, StringBuilder builder) {
            boolean bracketUsed = false;
            for (String envName : configuration.getEnvironments()) {
                final String envValue = System.getenv(envName);
                if (envValue == null && !configuration.isEnvironmentShowNullable()) {
                    continue;
                }

                if (!bracketUsed) {
                    builder.append('[');
                    bracketUsed = true;
                } else {
                    builder.append(", ");
                }

                if (configuration.isEnvironmentShowName()) {
                    builder.append(envName);
                    builder.append('=');
                }

                builder.append(envValue);
            }

            if (bracketUsed) {
                builder.append("] ");
            }
        }

        @Override
        public int order() {
            return LayoutOrder.ENVIRONMENT.ordinal();
        }
    }

    static final class ThreadLayout implements Layout {

        @Override
        public void print(String loggerName, int level, StringBuilder builder) {
            final String threadName = Thread.currentThread().getName();
            builder.append('[');
            builder.append(threadName);
            builder.append("] ");
        }

        @Override
        public int order() {
            return LayoutOrder.THREAD.ordinal();
        }
    }

    static final class LoggerNameLayout implements Layout {

        @Override
        public void print(String loggerName, int level, StringBuilder builder) {
            builder.append(loggerName);
        }

        @Override
        public int order() {
            return LayoutOrder.LOGGER_NAME.ordinal();
        }
    }
}
