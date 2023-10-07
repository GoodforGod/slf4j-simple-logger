package io.goodforgod.slf4j.simplelogger;

import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.MDC;

/**
 * JSON logger layout implementations
 *
 * @author Anton Kurako (GoodforGod)
 * @since 16.04.2022
 */
final class JsonLoggerLayouts {

    private JsonLoggerLayouts() {}

    interface SeparatorLayout extends Layout {

    }

    static final class DateTimeLayout extends SimpleLoggerLayouts.DateTimeLayout {

        DateTimeLayout(SimpleLoggerConfiguration configuration) {
            super(configuration);
        }

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append("\"timestamp\":\"");
            event.append(getEventTime(event));
            event.append('\"');
        }

        @Override
        public int order() {
            return SimpleLoggerLayouts.LayoutOrder.DATE_TIME.ordinal();
        }
    }

    static final class TimeLayout extends SimpleLoggerLayouts.TimeLayout {

        TimeLayout(SimpleLoggerConfiguration configuration) {
            super(configuration);
        }

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append("\"timestamp\":\"");
            event.append(getEventTime(event));
            event.append('\"');
        }

        @Override
        public int order() {
            return SimpleLoggerLayouts.LayoutOrder.DATE_TIME.ordinal();
        }
    }

    static final class UnixTimeLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append("\"timestamp\":\"");
            event.append(System.currentTimeMillis());
            event.append('\"');
        }

        @Override
        public int order() {
            return SimpleLoggerLayouts.LayoutOrder.DATE_TIME.ordinal();
        }
    }

    static final class MillisFromStartLayout implements Layout {

        private final SimpleLoggerConfiguration configuration;

        MillisFromStartLayout(SimpleLoggerConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append("\"timestamp\":\"");
            event.append(System.currentTimeMillis() - configuration.getInitializeTime());
            event.append('\"');
        }

        @Override
        public int order() {
            return SimpleLoggerLayouts.LayoutOrder.DATE_TIME.ordinal();
        }
    }

    static final class ImplementationLayout implements Layout {

        private final SimpleLoggerConfiguration configuration;

        ImplementationLayout(SimpleLoggerConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append("\"implementation\":\"");
            event.append(configuration.getImplementationVersion());
            event.append('\"');
        }

        @Override
        public int order() {
            return SimpleLoggerLayouts.LayoutOrder.IMPLEMENTATION.ordinal();
        }
    }

    static final class LevelLayout extends SimpleLoggerLayouts.LevelLayout {

        LevelLayout(String trace, String debug, String info, String warn, String error) {
            super(trace, debug, info, warn, error);
        }

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append("\"level\":\"");
            event.append(renderLevel(event.level()));
            event.append('\"');
        }

        @Override
        public int order() {
            return SimpleLoggerLayouts.LayoutOrder.LEVEL.ordinal();
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
            return SimpleLoggerLayouts.LayoutOrder.ENVIRONMENT.ordinal();
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
                                ? "{\"name\":\"" + envName + "\",\"value\":\"" + envValue + "\"}"
                                : "\"" + envValue + "\"";
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));

            event.append("\"environment\":[");
            event.append(environments);
            event.append(']');
        }

        @Override
        public int order() {
            return SimpleLoggerLayouts.LayoutOrder.ENVIRONMENT.ordinal();
        }
    }

    static final class MDCLayout implements SeparatorLayout {

        @Override
        public void print(SimpleLoggingEvent event) {
            final Map<String, String> mdc = MDC.getCopyOfContextMap();
            if (mdc != null && !mdc.isEmpty()) {
                event.append("\"context\":{");
                boolean isNotFirst = false;
                for (var entry : mdc.entrySet()) {
                    if (isNotFirst) {
                        event.append(',');
                    }
                    event.append('\"');
                    event.append(entry.getKey());
                    event.append('\"');
                    event.append(':');
                    event.append('\"');
                    event.append(entry.getValue());
                    event.append('\"');
                    isNotFirst = true;
                }
                event.append("},");
            }
        }

        @Override
        public int order() {
            return SimpleLoggerLayouts.LayoutOrder.MDC.ordinal();
        }
    }

    static final class ThreadLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append("\"thread\":\"");
            event.append(Thread.currentThread().getName());
            event.append('\"');
        }

        @Override
        public int order() {
            return SimpleLoggerLayouts.LayoutOrder.THREAD.ordinal();
        }
    }

    static final class LoggerNameLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append("\"logger\":\"");
            event.append(event.logger());
            event.append('\"');
        }

        @Override
        public int order() {
            return SimpleLoggerLayouts.LayoutOrder.LOGGER_NAME.ordinal();
        }
    }

    static final class MessageLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append("\"message\":\"");
            event.append(event.message());
            event.append('\"');
        }

        @Override
        public int order() {
            return SimpleLoggerLayouts.LayoutOrder.MESSAGE.ordinal();
        }
    }

    static final class JsonStartTokenLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append('{');
        }

        @Override
        public int order() {
            return SimpleLoggerLayouts.LayoutOrder.EVENT_SEPARATOR.ordinal();
        }
    }

    static final class JsonEndTokenLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append('}');
        }

        @Override
        public int order() {
            return SimpleLoggerLayouts.LayoutOrder.EVENT_SEPARATOR.ordinal();
        }
    }

    static final class JsonSeparatorLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            event.append(',');
        }

        @Override
        public int order() {
            return SimpleLoggerLayouts.LayoutOrder.EVENT_SEPARATOR.ordinal();
        }
    }

    static final class ThrowableLayout implements Layout {

        @Override
        public void print(SimpleLoggingEvent event) {
            final Throwable throwable = event.throwable();
            if (throwable != null) {
                event.append(",\"exception\":\"");
                event.append(throwable.getMessage());
                event.append("\",\"stacktrace\":[");
                printThrowable(throwable, event.getBuilder());
                event.append("]");
            }
        }

        private void printThrowable(Throwable throwable, StringBuilder builder) {
            // Guard against malicious overrides of Throwable.equals by using a Set with identity equality
            // semantics.
            final Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
            visited.add(throwable);

            final StackTraceElement[] traces = throwable.getStackTrace();
            final int last = traces.length - 1;
            for (int i = 0; i < traces.length; i++) {
                final StackTraceElement trace = traces[i];
                final String message = (i == 0)
                        ? throwable.getMessage()
                        : null;
                printTrace(trace, message, builder);

                if (i != last) {
                    builder.append(',');
                }
            }

            for (Throwable suppressed : throwable.getSuppressed()) {
                printEnclosedStackTrace(suppressed, builder, traces, visited);
            }

            final Throwable cause = throwable.getCause();
            if (cause != null) {
                printEnclosedStackTrace(cause, builder, traces, visited);
            }
        }

        /**
         * Print our stack trace as an enclosed exception for the specified stack trace.
         */
        private void printEnclosedStackTrace(Throwable throwable,
                                             StringBuilder builder,
                                             StackTraceElement[] enclosingTrace,
                                             Set<Throwable> visited) {
            if (visited.contains(throwable)) {
                builder.append("[CIRCULAR REFERENCE: ").append(throwable).append("]");
            } else {
                visited.add(throwable);
                // Compute number of frames in common between this and enclosing trace
                final StackTraceElement[] traces = throwable.getStackTrace();

                int m = traces.length - 1;
                int n = enclosingTrace.length - 1;
                while (m >= 0 && n >= 0 && traces[m].equals(enclosingTrace[n])) {
                    m--;
                    n--;
                }

                if (builder.length() != 0) {
                    builder.append(",");
                }

                for (int i = 0; i <= m; i++) {
                    final StackTraceElement trace = traces[i];
                    final String message = (i == 0)
                            ? throwable.getMessage()
                            : null;
                    printTrace(trace, message, builder);

                    if (i != m) {
                        builder.append(',');
                    }
                }

                for (Throwable suppressed : throwable.getSuppressed()) {
                    printEnclosedStackTrace(suppressed, builder, traces, visited);
                }

                final Throwable cause = throwable.getCause();
                if (cause != null) {
                    printEnclosedStackTrace(cause, builder, traces, visited);
                }
            }
        }

        private void printTrace(StackTraceElement trace, String message, StringBuilder builder) {
            final String methodName = trace.isNativeMethod()
                    ? "native " + trace.getMethodName()
                    : trace.getMethodName();

            if (message != null) {
                builder.append("{\"clazz\":\"")
                        .append(trace.getClassName())
                        .append("\",\"message\":\"")
                        .append(message)
                        .append("\",\"method\":\"")
                        .append(methodName)
                        .append(":")
                        .append(trace.getLineNumber())
                        .append("\"}");
            } else {
                builder.append("{\"clazz\":\"")
                        .append(trace.getClassName())
                        .append("\",\"method\":\"")
                        .append(methodName)
                        .append(":")
                        .append(trace.getLineNumber())
                        .append("\"}");
            }
        }

        @Override
        public int order() {
            return SimpleLoggerLayouts.LayoutOrder.THROWABLE.ordinal();
        }
    }
}
