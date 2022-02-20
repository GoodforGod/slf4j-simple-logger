package io.goodforgod.slf4j.simplelogger;

import static io.goodforgod.slf4j.simplelogger.SimpleLoggerProperties.*;

import io.goodforgod.slf4j.simplelogger.OutputChoice.OutputChoiceType;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import org.slf4j.event.Level;
import org.slf4j.helpers.Util;

/**
 * This class holds configuration values for {@link SimpleLogger}. The values are computed at
 * runtime. See {@link SimpleLogger} documentation for more information.
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

    private static final String DATETIME_FORMAT_DEFAULT = "uuuu-MM-dd'T'HH:mm:ss.SSS";
    private static final DateTimeFormatter FORMATTER_DEFAULT = DateTimeFormatter.ofPattern(DATETIME_FORMAT_DEFAULT);
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
    DateTimeFormatter dateFormatter = FORMATTER_DEFAULT;
    OutputChoice outputChoice = null;
    String warnLevelString = Level.WARN.name();

    private final Properties properties = new Properties();

    void init() {
        loadProperties();

        final String defaultLogLevelString = getStringProperty(DEFAULT_LOG_LEVEL, null);
        if (defaultLogLevelString != null) {
            this.defaultLogLevel = stringToLevel(defaultLogLevelString);
        }

        this.showLogName = getBooleanProperty(SHOW_LOG_NAME, SimpleLoggerConfiguration.SHOW_LOG_NAME_DEFAULT);
        this.showShortLogName = getBooleanProperty(SHOW_SHORT_LOG_NAME, SHOW_SHORT_LOG_NAME_DEFAULT);
        this.showDateTime = getBooleanProperty(SHOW_DATE_TIME, SHOW_DATE_TIME_DEFAULT);
        this.showThreadName = getBooleanProperty(SHOW_THREAD_NAME, SHOW_THREAD_NAME_DEFAULT);
        this.levelInBrackets = getBooleanProperty(LEVEL_IN_BRACKETS, LEVEL_IN_BRACKETS_DEFAULT);
        this.warnLevelString = getStringProperty(WARN_LEVEL_STRING, Level.WARN.name());
        this.logFile = getStringProperty(LOG_FILE, logFile);

        final boolean cacheOutputStream = getBooleanProperty(CACHE_OUTPUT_STREAM_STRING, CACHE_OUTPUT_STREAM_DEFAULT);
        this.outputChoice = computeOutputChoice(logFile, cacheOutputStream);

        final String dateTimeFormatStr = getStringProperty(DATETIME_FORMAT);
        if (dateTimeFormatStr != null) {
            try {
                this.dateFormatter = DateTimeFormatter.ofPattern(dateTimeFormatStr);
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
        return (prop == null)
                ? defaultValue
                : prop;
    }

    boolean getBooleanProperty(String name, boolean defaultValue) {
        final String prop = getStringProperty(name);
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
        return (prop == null)
                ? properties.getProperty(name)
                : prop;
    }

    static int stringToLevel(String levelStr) {
        final int lvl = stringToLevelOptimized(levelStr);
        if (lvl != -1) {
            return lvl;
        }

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

    private static int stringToLevelOptimized(String level) {
        switch (level) {
            case "TRACE":
                return SimpleLogger.LOG_LEVEL_TRACE;
            case "DEBUG":
                return SimpleLogger.LOG_LEVEL_DEBUG;
            case "INFO":
                return SimpleLogger.LOG_LEVEL_INFO;
            case "WARN":
                return SimpleLogger.LOG_LEVEL_WARN;
            case "ERROR":
                return SimpleLogger.LOG_LEVEL_ERROR;
            case "OFF":
                return SimpleLogger.LOG_LEVEL_OFF;
            default:
                return -1;
        }
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
            try (PrintStream printStream = new PrintStream(new FileOutputStream(logFile))) {
                return new OutputChoice(printStream);
            } catch (IOException e) {
                Util.report("Could not open [" + logFile + "]. Defaulting to System.err", e);
                return new OutputChoice(OutputChoiceType.SYS_ERR);
            }
        }
    }
}
