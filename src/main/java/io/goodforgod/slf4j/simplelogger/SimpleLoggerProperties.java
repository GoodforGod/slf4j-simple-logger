package io.goodforgod.slf4j.simplelogger;

/**
 * All system properties used by <code>SimpleLogger</code> start with this prefix
 *
 * @author Anton Kurako (GoodforGod)
 * @since 10.10.2021
 */
public final class SimpleLoggerProperties {

    private SimpleLoggerProperties() {}

    public static final String PREFIX = "org.slf4j.simpleLogger.";

    public static final String PREFIX_LOG = PREFIX + "log.";

    public static final String CACHE_OUTPUT_STREAM_STRING = PREFIX + "cacheOutputStream";
    public static final String WARN_LEVEL_STRING = PREFIX + "warnLevelString";
    public static final String LEVEL_IN_BRACKETS = PREFIX + "levelInBrackets";
    public static final String LOG_FILE = PREFIX + "logFile";
    public static final String SHOW_SHORT_LOG_NAME = PREFIX + "showShortLogName";
    public static final String SHOW_LOG_NAME = PREFIX + "showLogName";
    public static final String SHOW_THREAD_NAME = PREFIX + "showThreadName";
    public static final String DATETIME_FORMAT = PREFIX + "dateTimeFormat";
    public static final String SHOW_DATE_TIME = PREFIX + "showDateTime";
    public static final String DEFAULT_LOG_LEVEL = PREFIX + "defaultLogLevel";
}
