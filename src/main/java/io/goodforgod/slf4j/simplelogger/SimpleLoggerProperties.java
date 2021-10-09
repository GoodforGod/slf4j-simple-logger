package io.goodforgod.slf4j.simplelogger;

/**
 * All system properties used by <code>SimpleLogger</code> start with this
 * prefix
 *
 * @author Anton Kurako (GoodforGod)
 * @since 10.10.2021
 */
public interface SimpleLoggerProperties {

    String SYSTEM_PREFIX = "io.goodforgod.simpleLogger.";

    String LOG_KEY_PREFIX = SYSTEM_PREFIX + "log.";

    String CACHE_OUTPUT_STREAM_STRING_KEY = SYSTEM_PREFIX + "cacheOutputStream";
    String WARN_LEVEL_STRING_KEY = SYSTEM_PREFIX + "warnLevelString";
    String LEVEL_IN_BRACKETS_KEY = SYSTEM_PREFIX + "levelInBrackets";
    String LOG_FILE_KEY = SYSTEM_PREFIX + "logFile";
    String SHOW_SHORT_LOG_NAME_KEY = SYSTEM_PREFIX + "showShortLogName";
    String SHOW_LOG_NAME_KEY = SYSTEM_PREFIX + "showLogName";
    String SHOW_THREAD_NAME_KEY = SYSTEM_PREFIX + "showThreadName";
    String DATE_TIME_FORMAT_KEY = SYSTEM_PREFIX + "dateTimeFormat";
    String SHOW_DATE_TIME_KEY = SYSTEM_PREFIX + "showDateTime";
    String DEFAULT_LOG_LEVEL_KEY = SYSTEM_PREFIX + "defaultLogLevel";
}
