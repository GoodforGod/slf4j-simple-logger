package io.goodforgod.slf4j.simplelogger;

import io.goodforgod.slf4j.simplelogger.OutputChoice.OutputChoiceType;
import org.slf4j.event.Level;
import org.slf4j.helpers.Util;

import java.io.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * This class holds configuration values for {@link SimpleLogger}. The
 * values are computed at runtime. See {@link SimpleLogger} documentation for
 * more information.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author Scott Sanders
 * @author Rod Waldhoff
 * @author Robert Burrell Donkin
 * @author C&eacute;drik LIME
 * @author Anton Kurako (GoodforGod)
 * @since 09.10.2021
 */
public class SimpleLoggerConfiguration {

    private static final String CONFIGURATION_FILE = "simplelogger.properties";

    private static final String SYSTEM_ERR = "System.err";
    private static final String SYSTEM_OUT = "System.out";
    private static final String BOOLEAN_TRUE = "true";

    private static final String DATE_TIME_FORMAT_STR_DEFAULT = null;
    private static final int DEFAULT_LOG_LEVEL_DEFAULT = SimpleLogger.LOG_LEVEL_INFO;
    private static final boolean SHOW_DATE_TIME_DEFAULT = false;
    private static final boolean SHOW_THREAD_NAME_DEFAULT = true;
    private static final boolean SHOW_LOG_NAME_DEFAULT = true;
    private static final boolean SHOW_SHORT_LOG_NAME_DEFAULT = false;
    private static final boolean LEVEL_IN_BRACKETS_DEFAULT = false;
    private static final String LOG_FILE_DEFAULT = SYSTEM_ERR;
    private static final boolean CACHE_OUTPUT_STREAM_DEFAULT = false;

    private String logFile = LOG_FILE_DEFAULT;

    int defaultLogLevel = DEFAULT_LOG_LEVEL_DEFAULT;
    boolean showDateTime = SHOW_DATE_TIME_DEFAULT;
    boolean showThreadName = SHOW_THREAD_NAME_DEFAULT;
    boolean showLogName = SHOW_LOG_NAME_DEFAULT;
    boolean showShortLogName = SHOW_SHORT_LOG_NAME_DEFAULT;
    boolean levelInBrackets = LEVEL_IN_BRACKETS_DEFAULT;
    DateTimeFormatter dateFormatter = null;
    OutputChoice outputChoice = null;
    String warnLevelString = Level.WARN.name();

    private final Properties properties = new Properties();

    void init() {
        loadProperties();

        final String defaultLogLevelString = getStringProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, null);
        if (defaultLogLevelString != null)
            defaultLogLevel = stringToLevel(defaultLogLevelString);

        showLogName = getBooleanProperty(SimpleLogger.SHOW_LOG_NAME_KEY, SimpleLoggerConfiguration.SHOW_LOG_NAME_DEFAULT);
        showShortLogName = getBooleanProperty(SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, SHOW_SHORT_LOG_NAME_DEFAULT);
        showDateTime = getBooleanProperty(SimpleLogger.SHOW_DATE_TIME_KEY, SHOW_DATE_TIME_DEFAULT);
        showThreadName = getBooleanProperty(SimpleLogger.SHOW_THREAD_NAME_KEY, SHOW_THREAD_NAME_DEFAULT);
        levelInBrackets = getBooleanProperty(SimpleLogger.LEVEL_IN_BRACKETS_KEY, LEVEL_IN_BRACKETS_DEFAULT);
        warnLevelString = getStringProperty(SimpleLogger.WARN_LEVEL_STRING_KEY, Level.WARN.name());
        logFile = getStringProperty(SimpleLogger.LOG_FILE_KEY, logFile);

        final boolean cacheOutputStream = getBooleanProperty(SimpleLogger.CACHE_OUTPUT_STREAM_STRING_KEY, CACHE_OUTPUT_STREAM_DEFAULT);
        outputChoice = computeOutputChoice(logFile, cacheOutputStream);

        final String dateTimeFormatStr = getStringProperty(SimpleLogger.DATE_TIME_FORMAT_KEY, DATE_TIME_FORMAT_STR_DEFAULT);
        if (dateTimeFormatStr != null) {
            try {
                dateFormatter = DateTimeFormatter.ofPattern(dateTimeFormatStr);
            } catch (IllegalArgumentException e) {
                Util.report("Bad date format in " + CONFIGURATION_FILE + "; will output relative time", e);
            }
        }
    }

    private void loadProperties() {
        // Add props from the resource simplelogger.properties
        InputStream in = AccessController.doPrivileged((PrivilegedAction<InputStream>) () -> {
            ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
            if (threadCL != null) {
                return threadCL.getResourceAsStream(CONFIGURATION_FILE);
            } else {
                return ClassLoader.getSystemResourceAsStream(CONFIGURATION_FILE);
            }
        });

        if (null != in) {
            try (in) {
                properties.load(in);
            } catch (IOException e) {
                // ignored
            }
        }
    }

    String getStringProperty(String name, String defaultValue) {
        String prop = getStringProperty(name);
        return (prop == null) ? defaultValue : prop;
    }

    boolean getBooleanProperty(String name, boolean defaultValue) {
        String prop = getStringProperty(name);
        return (prop == null)
                ? defaultValue
                : BOOLEAN_TRUE.equalsIgnoreCase(prop);
    }

    String getStringProperty(String name) {
        String prop = null;
        try {
            prop = System.getProperty(name);
        } catch (SecurityException e) {
            // Ignore
        }
        return (prop == null) ? properties.getProperty(name) : prop;
    }

    static int stringToLevel(String levelStr) {
        if (Level.TRACE.name().equalsIgnoreCase(levelStr)) {
            return SimpleLogger.LOG_LEVEL_TRACE;
        } else if (Level.DEBUG.name().equalsIgnoreCase(levelStr)) {
            return SimpleLogger.LOG_LEVEL_DEBUG;
        } else if (Level.INFO.name().equalsIgnoreCase(levelStr)) {
            return SimpleLogger.LOG_LEVEL_INFO;
        } else if (Level.WARN.name().equalsIgnoreCase(levelStr)) {
            return SimpleLogger.LOG_LEVEL_WARN;
        } else if (Level.ERROR.name().equalsIgnoreCase(levelStr)) {
            return SimpleLogger.LOG_LEVEL_ERROR;
        } else if ("off".equalsIgnoreCase(levelStr)) {
            return SimpleLogger.LOG_LEVEL_OFF;
        }
        // assume INFO by default
        return SimpleLogger.LOG_LEVEL_INFO;
    }

    private static OutputChoice computeOutputChoice(String logFile, boolean cacheOutputStream) {
        if (SYSTEM_ERR.equalsIgnoreCase(logFile)) {
            if (cacheOutputStream) {
                return new OutputChoice(OutputChoiceType.CACHED_SYS_ERR);
            } else {
                return new OutputChoice(OutputChoiceType.SYS_ERR);
            }
        } else if (SYSTEM_OUT.equalsIgnoreCase(logFile)) {
            if (cacheOutputStream) {
                return new OutputChoice(OutputChoiceType.CACHED_SYS_OUT);
            } else {
                return new OutputChoice(OutputChoiceType.SYS_OUT);
            }
        } else {
            try(PrintStream printStream = new PrintStream(new FileOutputStream(logFile))) {
                return new OutputChoice(printStream);
            } catch (IOException e) {
                Util.report("Could not open [" + logFile + "]. Defaulting to System.err", e);
                return new OutputChoice(OutputChoiceType.SYS_ERR);
            }
        }
    }
}
