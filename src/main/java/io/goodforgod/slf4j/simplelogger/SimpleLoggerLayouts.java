package io.goodforgod.slf4j.simplelogger;

import java.io.PrintWriter;
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
        LOGGER_NAME,
        MESSAGE,
        EVENT_SEPARATOR,
        THROWABLE
    }

    private static final class DateTimeCache {

        private final String formatted;
        private final long epochMillis;
        private final long epochShort;

        private DateTimeCache(String formatted, long epochMillis, long epochShort) {
            this.formatted = formatted;
            this.epochMillis = epochMillis;
            this.epochShort = epochShort;
        }

        private DateTimeCache(String formatted, long epochMillis) {
            this(formatted, epochMillis, -1);
        }
    }

    private abstract static class AbstractTimeLayout implements Layout {

        final SimpleLoggerConfiguration configuration;
        DateTimeCache cache = new DateTimeCache(null, -1);

        AbstractTimeLayout(SimpleLoggerConfiguration configuration) {
            this.configuration = configuration;
        }

        abstract String getFormattedTime(long currentMillis);

        abstract boolean isFormatterDefault();

        String getCachedFormattedTime() {
            final DateTimeCache cacheLocal = this.cache;
            final long epochMilli = System.currentTimeMillis();
            if (cacheLocal.epochMillis == epochMilli) {
                return cacheLocal.formatted;
            }

            long epochShort = -1;
            final boolean isDefaultFormatter = isFormatterDefault();
            if (isDefaultFormatter) {
                epochShort = epochMilli / 10000;
                if (cacheLocal.epochShort == epochShort) {
                    final String secondsAndMillis = String.valueOf(epochMilli % 10000);
                    final char seconds = secondsAndMillis.charAt(0);
                    final String millis = secondsAndMillis.substring(1);
                    return cacheLocal.formatted + seconds + "." + millis;
                }
            }

            final String formatted = getFormattedTime(epochMilli);
            if (isDefaultFormatter) {
                final String timeUpToSeconds = formatted.substring(0, formatted.length() - 5);
                this.cache = new DateTimeCache(timeUpToSeconds, epochMilli, epochShort);
            } else {
                this.cache = new DateTimeCache(formatted, epochMilli);
            }

            return formatted;
        }
    }

    static final class DateTimeLayout extends AbstractTimeLayout {

        DateTimeLayout(SimpleLoggerConfiguration configuration) {
            super(configuration);
        }

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append(getCachedFormattedTime());
            event.append(' ');
        }

        @Override
        boolean isFormatterDefault() {
            return configuration.getDateTimeFormatter() == SimpleLoggerConfiguration.DATE_TIME_FORMATTER_DEFAULT;
        }

        /**
         * @param currentMillis from epoch
         * @see LocalTime#ofNanoOfDay(long)
         * @return formatter date time
         */
        @Override
        String getFormattedTime(long currentMillis) {
            final Instant now = Instant.ofEpochMilli(currentMillis);
            final Clock clock = configuration.getClock();
            final ZoneOffset offset = clock.getZone().getRules().getOffset(now);
            final long localSecond = now.getEpochSecond() + offset.getTotalSeconds(); // overflow caught later
            final long localEpochDay = Math.floorDiv(localSecond, SECONDS_PER_DAY);
            final int secsOfDay = Math.floorMod(localSecond, SECONDS_PER_DAY);
            final LocalDate date = LocalDate.ofEpochDay(localEpochDay);
            final LocalTime time = LocalTime.ofNanoOfDay(secsOfDay * NANOS_PER_SECOND + now.getNano());
            final LocalDateTime dateTime = LocalDateTime.of(date, time);

            return configuration.getDateTimeFormatter().format(dateTime);
        }

        @Override
        public int order() {
            return LayoutOrder.DATE_TIME.ordinal();
        }
    }

    static final class TimeLayout extends AbstractTimeLayout {

        TimeLayout(SimpleLoggerConfiguration configuration) {
            super(configuration);
        }

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append(getCachedFormattedTime());
            event.append(' ');
        }

        @Override
        boolean isFormatterDefault() {
            return configuration.getDateTimeFormatter() == SimpleLoggerConfiguration.TIME_FORMATTER_DEFAULT;
        }

        /**
         * @param currentMillis from epoch
         * @see LocalTime#ofInstant(Instant, ZoneId)
         * @return formatter date time
         */
        @Override
        String getFormattedTime(long currentMillis) {
            final Instant now = Instant.ofEpochMilli(currentMillis);
            final Clock clock = configuration.getClock();
            final ZoneOffset offset = clock.getZone().getRules().getOffset(now);
            final long localSecond = now.getEpochSecond() + offset.getTotalSeconds();
            final int secsOfDay = Math.floorMod(localSecond, SECONDS_PER_DAY);
            final LocalTime localTime = LocalTime.ofNanoOfDay(secsOfDay * NANOS_PER_SECOND + now.getNano());

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
            event.append("[")
                    .append(configuration.getImplementationVersion())
                    .append("] ");
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
        public void print(SimpleLoggingEvent event) {
            event.append(renderLevel(event.level()));
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
        public void print(SimpleLoggingEvent event) {
            event.append(configuration.getEnvironmentsOnStart());
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
            final String threadName = Thread.currentThread().getName();
            event.append('[');
            event.append(threadName);
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
            event.append(event.logger()).append(" - ");
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
                final StringBuilderWriter stringWriter = new StringBuilderWriter(event.builder());
                final PrintWriter printWriter = new PrintWriter(stringWriter);
                throwable.printStackTrace(printWriter);
            }
        }

        @Override
        public int order() {
            return LayoutOrder.THROWABLE.ordinal();
        }
    }
}
