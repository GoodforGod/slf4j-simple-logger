package io.goodforgod.slf4j.simplelogger;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 27.03.2022
 */
final class SimpleLoggingEvent {

    private final StringBuilder builder = new StringBuilder();

    private final String loggerName;
    private final int level;
    private final String message;
    private final Throwable throwable;

    SimpleLoggingEvent(String loggerName, int level, String message, Throwable throwable) {
        this.loggerName = loggerName;
        this.level = level;
        this.message = message;
        this.throwable = throwable;
    }

    StringBuilder builder() {
        return builder;
    }

    SimpleLoggingEvent append(CharSequence text) {
        builder.append(text);
        return this;
    }

    SimpleLoggingEvent append(char character) {
        builder.append(character);
        return this;
    }

    SimpleLoggingEvent append(long number) {
        builder.append(number);
        return this;
    }

    String logger() {
        return loggerName;
    }

    int level() {
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
