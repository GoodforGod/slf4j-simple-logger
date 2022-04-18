package io.goodforgod.slf4j.simplelogger;

import java.io.PrintWriter;
import org.slf4j.event.Level;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 27.03.2022
 */
final class SimpleLoggingEvent {

    private final StringBuilder builder = new StringBuilder();
    private final long created = System.currentTimeMillis();

    private final String loggerName;
    private final Level level;
    private final String message;
    private final Throwable throwable;

    SimpleLoggingEvent(String loggerName, Level level, String message, Throwable throwable) {
        this.loggerName = loggerName;
        this.level = level;
        this.message = message;
        this.throwable = throwable;
    }

    void append(Throwable throwable) {
        final StringBuilderWriter stringWriter = new StringBuilderWriter(builder);
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
    }

    StringBuilder getBuilder() {
        return builder;
    }

    void append(CharSequence text) {
        builder.append(text);
    }

    void append(char character) {
        builder.append(character);
    }

    void append(long number) {
        builder.append(number);
    }

    long created() {
        return created;
    }

    String logger() {
        return loggerName;
    }

    Level level() {
        return level;
    }

    String message() {
        return message;
    }

    Throwable throwable() {
        return throwable;
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
