package io.goodforgod.slf4j.simplelogger;

import static io.goodforgod.slf4j.simplelogger.SimpleLoggerProperties.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
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
final class SimpleLoggerConfiguration {

    private static final String CONFIGURATION_FILE = "simplelogger.properties";

    private static final String SYSTEM_ERR = "System.err";
    private static final String SYSTEM_OUT = "System.out";

    static final DateTimeFormatter DATE_TIME_FORMATTER_DEFAULT = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS");
    static final DateTimeFormatter TIME_FORMATTER_DEFAULT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static final boolean CACHE_OUTPUT_STREAM_DEFAULT = false;
    private static final boolean LEVEL_IN_BRACKETS_DEFAULT = true;
    private static final boolean SHOW_THREAD_NAME_DEFAULT = false;
    private static final boolean SHOW_LOG_NAME_DEFAULT = true;
    private static final boolean SHOW_IMPLEMENTATION_VERSION_DEFAULT = false;
    private static final boolean SHOW_SHORT_LOG_NAME_DEFAULT = false;
    private static final boolean SHOW_DATE_TIME_DEFAULT = true;

    private final Properties properties = new Properties();

    // Non changeable configuration
    private long initializeTime;
    private EventEncoder eventEncoder;
    private String implementationVersion;
    private EventWriter eventWriter;
    private EventWriter eventWriterWarn;
    private EventWriter eventWriterError;
    private String environmentsOnStartText;
    private String environmentsOnStartJson;

    // Changeable configuration
    private OutputFormat format;
    private ZoneId zoneId;
    private DateTimeOutputType dateTimeOutputType = DateTimeOutputType.DATE_TIME;
    private DateTimeFormatter dateTimeFormatter;
    private int defaultLogLevel = Level.INFO.toInt();
    private boolean showShortLogName = SHOW_SHORT_LOG_NAME_DEFAULT;
    private boolean showLogName = SHOW_LOG_NAME_DEFAULT;
    private Integer logNameLength;
    private List<String> environments;
    private boolean environmentShowName;
    private boolean environmentShowNullable;
    private boolean showMDC;

    private List<Layout> layouts;

    void init() {
        loadProperties();

        this.initializeTime = System.currentTimeMillis();
        this.implementationVersion = SimpleLoggerConfiguration.class.getPackage().getImplementationVersion();

        final String logFile = getStringProperty(LOG_FILE, SYSTEM_OUT);
        final String logFileWarn = getStringProperty(LOG_FILE_WARN, SYSTEM_OUT);
        final String logFileError = getStringProperty(LOG_FILE_ERROR, SYSTEM_OUT);

        this.eventEncoder = computeEventEncoder();
        final boolean cacheOutputStream = getBooleanProperty(CACHE_OUTPUT_STREAM_STRING, CACHE_OUTPUT_STREAM_DEFAULT);
        this.eventWriter = computeLoggerStream(computeOutputChoice(logFile, cacheOutputStream));
        this.eventWriterWarn = (logFile.equals(logFileWarn))
                ? eventWriter
                : computeLoggerStream(computeOutputChoice(logFileWarn, cacheOutputStream));

        if (logFile.equals(logFileError)) {
            this.eventWriterError = eventWriter;
        } else if (logFileWarn.equals(logFileError)) {
            this.eventWriterError = eventWriterWarn;
        } else {
            this.eventWriterError = computeLoggerStream(computeOutputChoice(logFileError, cacheOutputStream));
        }

        computeChangeableConfiguration();
        this.environmentsOnStartText = computeEnvironmentsOnStartText();
        this.environmentsOnStartJson = computeEnvironmentsOnStartJson();
        this.layouts = OutputFormat.TEXT.equals(format)
                ? computeTextLayouts()
                : computeJsonLayouts();
    }

    void refresh() {
        computeChangeableConfiguration();
        this.layouts = OutputFormat.TEXT.equals(format)
                ? computeTextLayouts()
                : computeJsonLayouts();
    }

    private void computeChangeableConfiguration() {
        this.format = computeOutputFormat();
        this.dateTimeOutputType = computeDateTimeOutputType();
        if (DateTimeOutputType.DATE_TIME.equals(dateTimeOutputType) || DateTimeOutputType.TIME.equals(dateTimeOutputType)) {
            this.dateTimeFormatter = getDateTimeFormatter(dateTimeOutputType);
        }

        this.zoneId = Optional.ofNullable(getStringProperty(ZONE_ID))
                .filter(value -> (!"null".equals(value)))
                .map(ZoneId::of)
                .orElse(null);

        final String defaultLogLevelString = getStringProperty(DEFAULT_LOG_LEVEL);
        if (defaultLogLevelString != null) {
            this.defaultLogLevel = tryStringToLevel(defaultLogLevelString).orElse(Level.INFO.toInt());
        }

        this.showShortLogName = getBooleanProperty(SHOW_SHORT_LOG_NAME, SHOW_SHORT_LOG_NAME_DEFAULT);
        this.showLogName = getBooleanProperty(SHOW_LOG_NAME, SimpleLoggerConfiguration.SHOW_LOG_NAME_DEFAULT);
        this.logNameLength = getIntProperty(SHOW_LOG_NAME_LENGTH)
                .filter(i -> i > 0)
                .orElse(null);

        this.environments = computeEnvironments();
        this.environmentShowName = getBooleanProperty(ENVIRONMENT_SHOW_NAME, true);
        this.environmentShowNullable = getBooleanProperty(ENVIRONMENT_SHOW_NULLABLE, false);
        this.showMDC = getBooleanProperty(SHOW_MDC, false);
    }

    /**
     * @return logger stream used for writing events
     */
    private EventWriter computeLoggerStream(OutputChoice outputChoice) {
        return new EventWriters.LockEventWriter(this, outputChoice);
    }

    private OutputFormat computeOutputFormat() {
        try {
            return OutputFormat.valueOf(getStringProperty(FORMAT, OutputFormat.TEXT.name()));
        } catch (Exception e) {
            Util.report("Invalid output format in " + CONFIGURATION_FILE + ", will output in TEXT format", e);
            return OutputFormat.TEXT;
        }
    }

    private EventEncoder computeEventEncoder() {
        return Optional.ofNullable(getStringProperty(CHARSET, null))
                .map(charset -> "null".equals(charset)
                        ? new EventEncoders.SimpleEventEncoder()
                        : new EventEncoders.CharsetEventEncoder(Charset.forName(charset)))
                .orElseGet(() -> new EventEncoders.CharsetEventEncoder(StandardCharsets.UTF_8));
    }

    private DateTimeOutputType computeDateTimeOutputType() {
        return Optional.ofNullable(getStringProperty(DATE_TIME_OUTPUT_TYPE))
                .map(s -> {
                    try {
                        return DateTimeOutputType.valueOf(s);
                    } catch (IllegalArgumentException e) {
                        return DateTimeOutputType.DATE_TIME;
                    }
                })
                .orElse(DateTimeOutputType.DATE_TIME);
    }

    String computeLogName(String name) {
        if (!showLogName) {
            return null;
        }

        return (logNameLength == null)
                ? name
                : ClassNameAbbreviator.abbreviate(name, logNameLength);
    }

    EventWriter getEventWriter(Level logLevel) {
        switch (logLevel) {
            case WARN:
                return eventWriterWarn;
            case ERROR:
                return eventWriterError;
            default:
                return eventWriter;
        }
    }

    private Layout getDateTimeTextLayout(DateTimeOutputType dateTimeOutputType) {
        switch (dateTimeOutputType) {
            case TIME:
                return new SimpleLoggerLayouts.TimeLayout(this);
            case DATE_TIME:
                return new SimpleLoggerLayouts.DateTimeLayout(this);
            case UNIX_TIME:
                return new SimpleLoggerLayouts.UnixTimeLayout();
            case MILLIS_FROM_START:
                return new SimpleLoggerLayouts.MillisFromStartLayout(this);
            default:
                throw new IllegalStateException("Unknown DateTimeOutputType: " + dateTimeOutputType);
        }
    }

    private Layout getDateTimeJsonLayout(DateTimeOutputType dateTimeOutputType) {
        switch (dateTimeOutputType) {
            case TIME:
                return new JsonLoggerLayouts.TimeLayout(this);
            case DATE_TIME:
                return new JsonLoggerLayouts.DateTimeLayout(this);
            case UNIX_TIME:
                return new JsonLoggerLayouts.UnixTimeLayout();
            case MILLIS_FROM_START:
                return new JsonLoggerLayouts.MillisFromStartLayout(this);
            default:
                throw new IllegalStateException("Unknown DateTimeOutputType: " + dateTimeOutputType);
        }
    }

    private List<String> computeEnvironments() {
        return Optional.ofNullable(getStringProperty(ENVIRONMENTS))
                .filter(envs -> !envs.isBlank())
                .map(envs -> List.copyOf(Arrays.stream(envs.split(","))
                        .map(String::strip)
                        .filter(env -> !env.isBlank())
                        .collect(Collectors.toList())))
                .orElse(Collections.emptyList());
    }

    private String computeEnvironmentsOnStartText() {
        final boolean rememberEnvsOnStart = getBooleanProperty(ENVIRONMENT_REMEMBER_ON_START, false);
        if (rememberEnvsOnStart) {
            final String envsOnStart = this.environments.stream()
                    .map(env -> {
                        final String envValue = System.getenv(env);
                        if (envValue == null && !this.environmentShowNullable) {
                            return null;
                        }

                        return this.environmentShowName
                                ? env + "=" + envValue
                                : envValue;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));

            if (!envsOnStart.isEmpty()) {
                return "[" + envsOnStart + "] ";
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private String computeEnvironmentsOnStartJson() {
        final boolean rememberEnvsOnStart = getBooleanProperty(ENVIRONMENT_REMEMBER_ON_START, false);
        if (rememberEnvsOnStart) {
            final List<String> envsOnStart = this.environments.stream()
                    .map(envName -> {
                        final String envValue = System.getenv(envName);
                        if (envValue == null && !environmentShowNullable) {
                            return null;
                        }

                        return (environmentShowName)
                                ? "{\"name\":\"" + envName + "\",\"value\":\"" + envValue + "\"}"
                                : "\"" + envValue + "\"";
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (envsOnStart.isEmpty()) {
                return null;
            } else {
                return envsOnStart.stream().collect(Collectors.joining(",", "\"environment\": [", "]"));
            }
        } else {
            return null;
        }
    }

    private DateTimeFormatter getDateTimeFormatter(DateTimeOutputType dateTimeOutputType) {
        final String dateTimeFormatStr = getStringProperty(DATE_TIME_FORMAT);
        if (dateTimeFormatStr != null) {
            try {
                final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormatStr);

                // check formatting in initialization
                if (DateTimeOutputType.DATE_TIME.equals(dateTimeOutputType)) {
                    formatter.format(LocalDateTime.now());
                } else if (DateTimeOutputType.TIME.equals(dateTimeOutputType)) {
                    formatter.format(LocalTime.now());
                }

                return formatter;
            } catch (IllegalArgumentException e) {
                Util.report("Invalid date output format in " + CONFIGURATION_FILE + ", will output in DATE_TIME format", e);
            }
        }

        if (DateTimeOutputType.DATE_TIME.equals(dateTimeOutputType)) {
            return DATE_TIME_FORMATTER_DEFAULT;
        } else if (DateTimeOutputType.TIME.equals(dateTimeOutputType)) {
            return TIME_FORMATTER_DEFAULT;
        } else {
            throw new UnsupportedOperationException("Unsupported Date Output Type formatter: " + dateTimeOutputType);
        }
    }

    private List<Layout> computeTextLayouts() {
        final List<Layout> loggerLayouts = new ArrayList<>();
        final boolean showDateTime = getBooleanProperty(SHOW_DATE_TIME, SHOW_DATE_TIME_DEFAULT);
        if (showDateTime) {
            loggerLayouts.add(getDateTimeTextLayout(dateTimeOutputType));
        }

        final boolean showImplementationVersion = getBooleanProperty(SHOW_IMPLEMENTATION_VERSION,
                SHOW_IMPLEMENTATION_VERSION_DEFAULT)
                && this.implementationVersion != null
                && !"null".equalsIgnoreCase(this.implementationVersion);
        if (showImplementationVersion) {
            loggerLayouts.add(new SimpleLoggerLayouts.ImplementationLayout(this));
        }

        final boolean showThreadName = getBooleanProperty(SHOW_THREAD_NAME, SHOW_THREAD_NAME_DEFAULT);
        if (showThreadName) {
            loggerLayouts.add(new SimpleLoggerLayouts.ThreadLayout());
        }

        if (environmentsOnStartText != null) {
            loggerLayouts.add(new SimpleLoggerLayouts.EnvironmentOnStartLayout(this));
        } else if (!environments.isEmpty()) {
            loggerLayouts.add(new SimpleLoggerLayouts.EnvironmentLayout(this));
        }

        if (showMDC) {
            loggerLayouts.add(new SimpleLoggerLayouts.MDCLayout());
        }

        final boolean levelInBrackets = getBooleanProperty(LEVEL_IN_BRACKETS, LEVEL_IN_BRACKETS_DEFAULT);
        if (levelInBrackets) {
            loggerLayouts.add(new SimpleLoggerLayouts.LevelLayout("[TRACE] ", "[DEBUG] ", "[INFO] ", "[WARN] ", "[ERROR] "));
        } else {
            loggerLayouts.add(new SimpleLoggerLayouts.LevelLayout("TRACE ", "DEBUG ", "INFO ", "WARN ", "ERROR "));
        }

        if (showShortLogName || showLogName) {
            loggerLayouts.add(new SimpleLoggerLayouts.LoggerNameLayout());
        }

        loggerLayouts.add(new SimpleLoggerLayouts.MessageLayout());
        loggerLayouts.add(new SimpleLoggerLayouts.SeparatorLayout());
        loggerLayouts.add(new SimpleLoggerLayouts.ThrowableLayout());

        Collections.sort(loggerLayouts);
        return Collections.unmodifiableList(loggerLayouts);
    }

    private List<Layout> computeJsonLayouts() {
        final List<Layout> loggerLayouts = new ArrayList<>();
        final boolean showDateTime = getBooleanProperty(SHOW_DATE_TIME, SHOW_DATE_TIME_DEFAULT);
        if (showDateTime) {
            loggerLayouts.add(getDateTimeJsonLayout(dateTimeOutputType));
        }

        final boolean showImplementationVersion = getBooleanProperty(SHOW_IMPLEMENTATION_VERSION,
                SHOW_IMPLEMENTATION_VERSION_DEFAULT)
                && this.implementationVersion != null
                && !"null".equalsIgnoreCase(this.implementationVersion);
        if (showImplementationVersion) {
            loggerLayouts.add(new JsonLoggerLayouts.ImplementationLayout(this));
        }

        final boolean showThreadName = getBooleanProperty(SHOW_THREAD_NAME, SHOW_THREAD_NAME_DEFAULT);
        if (showThreadName) {
            loggerLayouts.add(new JsonLoggerLayouts.ThreadLayout());
        }

        if (environmentsOnStartJson != null) {
            loggerLayouts.add(new JsonLoggerLayouts.EnvironmentOnStartLayout(this));
        } else if (!environments.isEmpty()) {
            loggerLayouts.add(new JsonLoggerLayouts.EnvironmentLayout(this));
        }

        if (showMDC) {
            loggerLayouts.add(new JsonLoggerLayouts.MDCLayout());
        }

        loggerLayouts.add(new JsonLoggerLayouts.LevelLayout("TRACE", "DEBUG", "INFO", "WARN", "ERROR"));
        if (showShortLogName || showLogName) {
            loggerLayouts.add(new JsonLoggerLayouts.LoggerNameLayout());
        }

        loggerLayouts.add(new JsonLoggerLayouts.MessageLayout());
        loggerLayouts.add(new JsonLoggerLayouts.ThrowableLayout());

        Collections.sort(loggerLayouts);

        final List<Layout> jsonLayouts = new ArrayList<>();
        jsonLayouts.add(new JsonLoggerLayouts.JsonStartTokenLayout());
        for (int i = 0; i < loggerLayouts.size(); i++) {
            jsonLayouts.add(loggerLayouts.get(i));

            // skip separate token before JsonLoggerLayouts.ThrowableLayout
            if (i + 1 == loggerLayouts.size()) {
                jsonLayouts.add(new JsonLoggerLayouts.JsonEndTokenLayout());
            } else if (i + 2 < loggerLayouts.size()) {
                jsonLayouts.add(new JsonLoggerLayouts.JsonSeparatorLayout());
            }
        }

        return Collections.unmodifiableList(jsonLayouts);
    }

    ZoneId getZoneId() {
        return (zoneId == null)
                ? ZoneId.systemDefault()
                : zoneId;
    }

    List<String> getEnvironments() {
        return environments;
    }

    String getEnvironmentsOnStartText() {
        return environmentsOnStartText;
    }

    String getEnvironmentsOnStartJson() {
        return environmentsOnStartJson;
    }

    boolean isEnvironmentShowNullable() {
        return environmentShowNullable;
    }

    boolean isEnvironmentShowName() {
        return environmentShowName;
    }

    boolean showMDC() {
        return showMDC;
    }

    List<Layout> getLayouts() {
        return layouts;
    }

    String getImplementationVersion() {
        return implementationVersion;
    }

    DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    long getInitializeTime() {
        return initializeTime;
    }

    int getDefaultLogLevel() {
        return defaultLogLevel;
    }

    boolean isShowShortLogName() {
        return showShortLogName;
    }

    EventEncoder getEventEncoder() {
        return eventEncoder;
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
        final String prop = getStringProperty(name);
        return (prop == null)
                ? defaultValue
                : prop;
    }

    String getStringProperty(String name) {
        String prop = null;
        try {
            prop = System.getProperty(name);
        } catch (SecurityException e) {
            // Ignore
        }

        final String value = (prop == null)
                ? properties.getProperty(name)
                : prop;

        if (isEnvironmentValue(value)) {
            final String envProperty = value.substring(2, value.length() - 1);
            final String[] environmentAndDefault = envProperty.split(":");

            if (environmentAndDefault.length > 2) {
                throw new IllegalArgumentException(
                        "System Environment property can't contain only 1 ':' symbol but had: " + envProperty);
            } else if (environmentAndDefault.length == 2) {
                final String envValue = System.getenv(environmentAndDefault[0]);
                if (envValue == null) {
                    return (environmentAndDefault[1].isBlank())
                            ? null
                            : environmentAndDefault[1];
                }

                return envValue;
            }

            return System.getenv(environmentAndDefault[0]);
        } else {
            return value;
        }
    }

    private boolean getBooleanProperty(String name, boolean defaultValue) {
        final String prop = getStringProperty(name);
        return (prop == null)
                ? defaultValue
                : "true".equalsIgnoreCase(prop);
    }

    private Optional<Integer> getIntProperty(String name) {
        final String prop = getStringProperty(name);
        if (prop == null)
            return Optional.empty();

        try {
            return Optional.of(Integer.parseInt(prop));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static boolean isEnvironmentValue(String value) {
        return value != null && value.startsWith("${") && value.endsWith("}");
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
            return Level.TRACE.toInt();
        } else if (Level.DEBUG.name().equalsIgnoreCase(levelStr)) {
            return Level.DEBUG.toInt();
        } else if (Level.INFO.name().equalsIgnoreCase(levelStr)) {
            return Level.INFO.toInt();
        } else if (Level.WARN.name().equalsIgnoreCase(levelStr)) {
            return Level.WARN.toInt();
        } else if (Level.ERROR.name().equalsIgnoreCase(levelStr)) {
            return Level.ERROR.toInt();
        } else if ("off".equalsIgnoreCase(levelStr)) {
            return SimpleLogger.LOG_LEVEL_OFF;
        }

        return null;
    }

    private static int stringToLevelOptimized(String level) {
        switch (level) {
            case "TRACE":
                return Level.TRACE.toInt();
            case "DEBUG":
                return Level.DEBUG.toInt();
            case "INFO":
                return Level.INFO.toInt();
            case "WARN":
                return Level.WARN.toInt();
            case "ERROR":
                return Level.ERROR.toInt();
            case "OFF":
                return SimpleLogger.LOG_LEVEL_OFF;
            default:
                return -1;
        }
    }

    private static OutputChoice computeOutputChoice(String logFile, boolean cacheOutputStream) {
        if (SYSTEM_ERR.equalsIgnoreCase(logFile)) {
            if (cacheOutputStream) {
                return new OutputChoices.CachedSystemErrOutputChoice();
            } else {
                return new OutputChoices.SystemErrOutputChoice();
            }
        } else if (SYSTEM_OUT.equalsIgnoreCase(logFile)) {
            if (cacheOutputStream) {
                return new OutputChoices.CachedSystemOutOutputChoice();
            } else {
                return new OutputChoices.SystemOutOutputChoice();
            }
        } else {
            try {
                final PrintStream printStream = new PrintStream(new FileOutputStream(logFile), true);
                return new OutputChoices.FileOutputChoice(printStream);
            } catch (IOException e) {
                Util.report("Could not open [" + logFile + "]. Defaulting to System.err", e);
                return new OutputChoices.SystemErrOutputChoice();
            }
        }
    }
}
