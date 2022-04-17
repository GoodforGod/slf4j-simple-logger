package io.goodforgod.slf4j.simplelogger;

import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.event.Level;

/**
 * JSON logger layout implementations
 *
 * @author Anton Kurako (GoodforGod)
 * @since 16.04.2022
 */
final class JsonLoggerLayouts {

    private JsonLoggerLayouts() {}

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

    static final class DateTimeLayout extends SimpleLoggerLayouts.DateTimeLayout {

        DateTimeLayout(SimpleLoggerConfiguration configuration) {
            super(configuration);
        }

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append("\"timestamp\": \"");
            event.append(getEventTime(event));
            event.append("\"");
        }

        @Override
        public int order() {
            return LayoutOrder.DATE_TIME.ordinal();
        }
    }

    static final class TimeLayout extends SimpleLoggerLayouts.TimeLayout {

        TimeLayout(SimpleLoggerConfiguration configuration) {
            super(configuration);
        }

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append("\"timestamp\": \"");
            event.append(getEventTime(event));
            event.append("\"");
        }

        @Override
        public int order() {
            return LayoutOrder.DATE_TIME.ordinal();
        }
    }

    static final class UnixTimeLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append("\"timestamp\": \"");
            event.append(System.currentTimeMillis());
            event.append("\"");
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
            event.append("\"timestamp\": \"");
            event.append(System.currentTimeMillis() - configuration.getInitializeTime());
            event.append("\"");
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
            event.append("\"implementation\": \"");
            event.append(configuration.getImplementationVersion());
            event.append("\"");
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
            event.append("\"level\": \"");
            event.append(renderLevel(event.level()));
            event.append("\"");
        }

        private String renderLevel(Level level) {
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
            event.append(configuration.getEnvironmentsOnStartJson());
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
            final String environments = configuration.getEnvironments().stream()
                    .map(envName -> {
                        final String envValue = System.getenv(envName);
                        if (envValue == null && !configuration.isEnvironmentShowNullable()) {
                            return null;
                        }

                        return (configuration.isEnvironmentShowName())
                                ? "\"" + envName + "\": \"" + envValue + "\""
                                : "\"" + envValue + "\"";
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));

            if (configuration.isEnvironmentShowName()) {
                event.append("\"environments\": {");
                event.append(environments);
                event.append("}");
            } else {
                event.append("\"environments\": [");
                event.append(environments);
                event.append("]");
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
            event.append("\"thread\": \"");
            event.append(Thread.currentThread().getName());
            event.append("\"");
        }

        @Override
        public int order() {
            return LayoutOrder.THREAD.ordinal();
        }
    }

    static final class LoggerNameLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append("\"logger\": \"");
            event.append(event.logger());
            event.append("\"");
        }

        @Override
        public int order() {
            return LayoutOrder.LOGGER_NAME.ordinal();
        }
    }

    static final class MessageLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append("\"message\": \"");
            event.append(event.message());
            event.append("\"");
        }

        @Override
        public int order() {
            return LayoutOrder.MESSAGE.ordinal();
        }
    }

    static final class JsonStartTokenLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append("{");
        }

        @Override
        public int order() {
            return LayoutOrder.EVENT_SEPARATOR.ordinal();
        }
    }

    static final class JsonEndTokenLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append("}");
        }

        @Override
        public int order() {
            return LayoutOrder.EVENT_SEPARATOR.ordinal();
        }
    }

    static final class JsonSeparatorLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append(",");
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
                event.append("\"throwable\": \"");
                event.append(throwable);
                event.append("\"");
            }
        }

        @Override
        public int order() {
            return LayoutOrder.THROWABLE.ordinal();
        }
    }
}
