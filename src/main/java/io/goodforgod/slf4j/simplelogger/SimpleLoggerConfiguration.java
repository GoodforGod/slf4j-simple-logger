package io.goodforgod.slf4j.simplelogger;

import static io.goodforgod.slf4j.simplelogger.SimpleLoggerProperties.*;

import io.goodforgod.slf4j.simplelogger.OutputChoice.OutputChoiceType;
import java.io.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
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
    private static final String LOG_FILE_DEFAULT = SYSTEM_ERR;
    private static final boolean CACHE_OUTPUT_STREAM_DEFAULT = false;

    private static final boolean LEVEL_IN_BRACKETS_DEFAULT = true;
    private static final boolean SHOW_THREAD_NAME_DEFAULT = false;
    private static final boolean SHOW_LOG_NAME_DEFAULT = true;
    private static final boolean SHOW_IMPLEMENTATION_VERSION_DEFAULT = false;
    private static final boolean SHOW_SHORT_LOG_NAME_DEFAULT = false;

    private static final boolean SHOW_DATE_TIME_DEFAULT = true;

    final long initializeTime = System.currentTimeMillis();

    private String logFile = LOG_FILE_DEFAULT;

    OutputChoice outputChoice = null;
    int defaultLogLevel = SimpleLogger.LOG_LEVEL_INFO;

    boolean levelInBrackets = LEVEL_IN_BRACKETS_DEFAULT;
    boolean showThreadName = SHOW_THREAD_NAME_DEFAULT;
    boolean showLogName = SHOW_LOG_NAME_DEFAULT;
    boolean showImplementationVersion = SHOW_IMPLEMENTATION_VERSION_DEFAULT;
    Integer logNameLength = null;
    boolean showShortLogName = SHOW_SHORT_LOG_NAME_DEFAULT;
    boolean showDateTime = SHOW_DATE_TIME_DEFAULT;
    DateTimeFormatter dateFormatter = FORMATTER_DEFAULT;
    DateTimeOutputType dateTimeOutputType = DateTimeOutputType.DATE_TIME;
    String implementationVersion = SimpleLoggerConfiguration.class.getPackage().getImplementationVersion();

    private final Properties properties = new Properties();

    void init() {
        loadProperties();

        final String defaultLogLevelString = getStringProperty(DEFAULT_LOG_LEVEL, null);
        if (defaultLogLevelString != null) {
            this.defaultLogLevel = tryStringToLevel(defaultLogLevelString).orElse(SimpleLogger.LOG_LEVEL_INFO);
        }

        this.showImplementationVersion = getBooleanProperty(SHOW_IMPLEMENTATION_VERSION, SHOW_IMPLEMENTATION_VERSION_DEFAULT)
                && implementationVersion != null
                && !"null".equalsIgnoreCase(implementationVersion);
        this.levelInBrackets = getBooleanProperty(LEVEL_IN_BRACKETS, LEVEL_IN_BRACKETS_DEFAULT);
        this.showLogName = getBooleanProperty(SHOW_LOG_NAME, SimpleLoggerConfiguration.SHOW_LOG_NAME_DEFAULT);
        this.showShortLogName = getBooleanProperty(SHOW_SHORT_LOG_NAME, SHOW_SHORT_LOG_NAME_DEFAULT);
        this.showThreadName = getBooleanProperty(SHOW_THREAD_NAME, SHOW_THREAD_NAME_DEFAULT);
        this.logNameLength = getIntProperty(SHOW_LOG_NAME_LENGTH)
                .filter(i -> i > 0)
                .orElse(null);

        this.logFile = getStringProperty(LOG_FILE, logFile);
        final boolean cacheOutputStream = getBooleanProperty(CACHE_OUTPUT_STREAM_STRING, CACHE_OUTPUT_STREAM_DEFAULT);
        this.outputChoice = computeOutputChoice(logFile, cacheOutputStream);

        this.showDateTime = getBooleanProperty(SHOW_DATE_TIME, SHOW_DATE_TIME_DEFAULT);
        this.dateTimeOutputType = Optional.ofNullable(getStringProperty(DATE_TIME_OUTPUT_TYPE))
                .map(s -> {
                    try {
                        return DateTimeOutputType.valueOf(s);
                    } catch (IllegalArgumentException e) {
                        return DateTimeOutputType.DATE_TIME;
                    }
                })
                .orElse(DateTimeOutputType.DATE_TIME);

        if (DateTimeOutputType.DATE_TIME.equals(this.dateTimeOutputType)) {
            final String dateTimeFormatStr = getStringProperty(DATE_TIME_FORMAT);
            if (dateTimeFormatStr != null) {
                try {
                    this.dateFormatter = DateTimeFormatter.ofPattern(dateTimeFormatStr);
                } catch (IllegalArgumentException e) {
                    Util.report("Bad date format in " + CONFIGURATION_FILE + "; will output relative time", e);
                }
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

    Optional<Integer> getIntProperty(String name) {
        final String prop = getStringProperty(name);
        if (prop == null)
            return Optional.empty();

        try {
            return Optional.of(Integer.parseInt(prop));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
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

    static Optional<Integer> tryStringToLevel(String levelStr) {
        return Optional.ofNullable(stringToLevel(levelStr));
    }

    static Integer stringToLevel(String levelStr) {
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

        return null;
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
            try (PrintStream printStream = new PrintStream(new FileOutputStream(logFile), true)) {
                return new OutputChoice(printStream);
            } catch (IOException e) {
                Util.report("Could not open [" + logFile + "]. Defaulting to System.err", e);
                return new OutputChoice(OutputChoiceType.SYS_ERR);
            }
        }
    }
}
