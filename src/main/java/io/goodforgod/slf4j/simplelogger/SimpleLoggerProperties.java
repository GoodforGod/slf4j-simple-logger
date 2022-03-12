package io.goodforgod.slf4j.simplelogger;

/**
 * All system properties used by <code>SimpleLogger</code> start with this prefix
 *
 * @author Anton Kurako (GoodforGod)
 * @since 10.10.2021
 */
public final class SimpleLoggerProperties {

    private SimpleLoggerProperties() {}

    public enum DateTimeOutputType {
        TIME,
        DATE_TIME,
        UNIX_TIME,
        MILLIS_FROM_START
    }

    private static final String PREFIX = "org.slf4j.simpleLogger.";

    public static final String PREFIX_LOG = PREFIX + "log.";

    public static final String CACHE_OUTPUT_STREAM_STRING = PREFIX + "cacheOutputStream";
    public static final String LOG_FILE = PREFIX + "logFile";
    public static final String LOG_FILE_WARN = PREFIX + "logFileWarn";
    public static final String LOG_FILE_ERROR = PREFIX + "logFileError";

    public static final String CHARSET = PREFIX + "charset";

    public static final String LEVEL_IN_BRACKETS = PREFIX + "levelInBrackets";
    public static final String SHOW_SHORT_LOG_NAME = PREFIX + "showShortLogName";
    public static final String SHOW_LOG_NAME = PREFIX + "showLogName";
    public static final String SHOW_LOG_NAME_LENGTH = PREFIX + "logNameLength";
    public static final String SHOW_THREAD_NAME = PREFIX + "showThreadName";
    public static final String SHOW_IMPLEMENTATION_VERSION = PREFIX + "showImplementationVersion";

    public static final String ENVIRONMENTS = PREFIX + "environments";
    public static final String ENVIRONMENT_SHOW_NULLABLE = PREFIX + "environmentShowNullable";
    public static final String ENVIRONMENT_SHOW_NAME = PREFIX + "environmentShowName";
    public static final String ENVIRONMENT_REMEMBER_ON_START = PREFIX + "environmentRememberOnStart";

    public static final String SHOW_DATE_TIME = PREFIX + "showDateTime";
    public static final String DATE_TIME_FORMAT = PREFIX + "dateTimeFormat";
    public static final String DATE_TIME_OUTPUT_TYPE = PREFIX + "dateTimeOutputType";

    public static final String DEFAULT_LOG_LEVEL = PREFIX + "defaultLogLevel";
}
